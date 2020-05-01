package com.leyou.service.Impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.leyou.common.dto.CartDTO;
import com.leyou.common.enums.ExceptionEnum;
import com.leyou.common.exception.JimmyException;
import com.leyou.common.vo.PageResult;
import com.leyou.dao.SkuDao;
import com.leyou.dao.SpuDao;
import com.leyou.dao.SpuDetailDao;
import com.leyou.dao.StockDao;
import com.leyou.domain.*;
import com.leyou.service.BrandService;
import com.leyou.service.CategoryService;
import com.leyou.service.GoodsService;
import org.apache.commons.lang.StringUtils;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import tk.mybatis.mapper.entity.Example;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class GoodsServiceImpl implements GoodsService {
    @Autowired
    private SpuDao spuDao;
    @Autowired
    private SpuDetailDao spuDetailDao;
    @Autowired
    private SkuDao skuDao;
    @Autowired
    private StockDao stockDao;
    @Autowired
    private CategoryService categoryService;
    @Autowired
    private BrandService brandService;
    @Autowired
    private AmqpTemplate amqpTemplate;

    @Override
    public PageResult<Spu> querySpuByPage(Integer page, Integer rows, Boolean saleable, String key) {
        PageHelper.startPage(page,rows);
        Example example = new Example(Spu.class);
        Example.Criteria criteria = example.createCriteria();
        if (StringUtils.isNotBlank(key)){
            criteria.andLike("title","%"+key+"%");
        }
        if (saleable!=null){
            criteria.andEqualTo("saleable",saleable);
        }
        //默认根据商品更新时间排序
        example.setOrderByClause("last_update_time DESC");
        List<Spu> spus = spuDao.selectByExample(example);
        if (CollectionUtils.isEmpty(spus)){
            throw new JimmyException(ExceptionEnum.GOODS_NOT_FOUND);
        }

        loadCategoryAndBrandName(spus);

        PageInfo<Spu> pageInfo = new PageInfo<>(spus);
        return new PageResult<>(pageInfo.getTotal(),spus);
    }

    @Override
    @Transactional
    public void saveGoods(Spu spu) {
        //spu
        spu.setId(null);
        spu.setCreateTime(new Date());
        spu.setLastUpdateTime(spu.getCreateTime());
        spu.setSaleable(true);
        spu.setValid(false);
        int count = spuDao.insert(spu);
        if (count!=1){
            throw new JimmyException(ExceptionEnum.GOODS_ADD_ERROR);
        }
        //spuDetail
        SpuDetail spuDetail = spu.getSpuDetail();
        spuDetail.setSpuId(spu.getId());
        spuDetailDao.insert(spuDetail);

        saveSkuAndStockBySpu(spu);

        //发送amqp消息
        amqpTemplate.convertAndSend("item.insert",spu.getId());

    }

    @Override
    public SpuDetail querySpuDetailById(Long spuId) {
        SpuDetail spuDetail = spuDetailDao.selectByPrimaryKey(spuId);
        if (spuDetail==null){
            throw new JimmyException(ExceptionEnum.GOODS_DETAIL_NOT_FOUND);
        }
        return spuDetail;
    }


    @Override
    public List<Sku> querySkuBySpuId(Long spuId) {
        //查询sku
        Sku sku = new Sku();
        sku.setSpuId(spuId);
        List<Sku> skus = skuDao.select(sku);
        if (CollectionUtils.isEmpty(skus)){
            throw new JimmyException(ExceptionEnum.GOODS_SKU_NOT_FOUND);
        }

        //查询sku中的stock

//        for (Sku s : skus) {
//            Stock stock = stockDao.selectByPrimaryKey(s.getId());
//            if (stock==null){
//                throw new JimmyException(ExceptionEnum.GOODS_STOCK_NOT_FOUND);
//            }
//            s.setStock(stock.getStock());
//        }

        //jdk1.8
        List<Long> ids = skus.stream().map(Sku::getId).collect(Collectors.toList());
        List<Stock> stocks = stockDao.selectByIdList(ids);
        if (CollectionUtils.isEmpty(stocks)){
            throw new JimmyException(ExceptionEnum.GOODS_STOCK_NOT_FOUND);
        }
        Map<Long, Integer> stockMap = stocks.stream().collect(Collectors.toMap(Stock::getSkuId, Stock::getStock));
        skus.forEach(s->s.setStock(stockMap.get(s.getId())));
        return skus;
    }

    @Override
    @Transactional
    public void updateGoods(Spu spu) {
        if (spu.getId()==null){
            throw new JimmyException(ExceptionEnum.ID_CANNOT_BE_NULL);
        }
        Sku sku = new Sku();
        sku.setSpuId(spu.getId());
        List<Sku> skus = skuDao.select(sku);
        if (!CollectionUtils.isEmpty(skus)){
            skuDao.delete(sku);
            List<Long> ids = skus.stream().map(Sku::getId).collect(Collectors.toList());
            stockDao.deleteByIdList(ids);
        }
        spu.setValid(null);
        spu.setSaleable(null);
        spu.setLastUpdateTime(new Date());
        spu.setCreateTime(null);
        int count = spuDao.updateByPrimaryKeySelective(spu);
        if (count!=1){
            throw new JimmyException(ExceptionEnum.GOODS_UPDATE_ERROR);
        }
        count = spuDetailDao.updateByPrimaryKeySelective(spu.getSpuDetail());
        if (count!=1){
            throw new JimmyException(ExceptionEnum.GOODS_UPDATE_ERROR);
        }

        saveSkuAndStockBySpu(spu);

        //发送amqp消息
        amqpTemplate.convertAndSend("item.update",spu.getId());
    }

    @Override
    public Spu querySpuByID(Long spuId) {
        //spu
        Spu spu = spuDao.selectByPrimaryKey(spuId);
        if (spu==null){
            throw new JimmyException(ExceptionEnum.GOODS_NOT_FOUND);
        }
        //sku
        List<Sku> skus = querySkuBySpuId(spuId);
        spu.setSkus(skus);
        //spuDetail
        SpuDetail spuDetail = querySpuDetailById(spuId);
        spu.setSpuDetail(spuDetail);
        return spu;
    }

    @Override
    @Transactional
    public void decreaseStock(List<CartDTO> carts) {
        /*TODO  x悲观锁：分布式锁(单线程串行执行),
                √乐观锁：不做判断直接减，在sql中判断stock>0
        */
        for (CartDTO cart : carts) {
            int count = stockDao.decreaseStock(cart.getSkuId(), cart.getNum());
            if (count!=1){
                throw new JimmyException(ExceptionEnum.STOCK_NOT_ENOUGH);
            }
        }
    }

    private void saveSkuAndStockBySpu(Spu spu){
        //sku
        List<Stock> stocks=new ArrayList<>();
        List<Sku> skus = spu.getSkus();
        for (Sku sku : skus) {
            sku.setCreateTime(new Date());
            sku.setLastUpdateTime(sku.getCreateTime());
            sku.setSpuId(spu.getId());
            int count = skuDao.insert(sku);
            if (count!=1){
                throw new JimmyException(ExceptionEnum.GOODS_ADD_ERROR);
            }
            //stock
            Stock stock = new Stock();
            stock.setSkuId(sku.getId());
            stock.setSeckillStock(null);
            stock.setSeckillTotal(null);
            stock.setStock(sku.getStock());
            stocks.add(stock);
        }
        //批量添加stock
        int count = stockDao.insertList(stocks);
        if (count!=stocks.size()){
            throw new JimmyException(ExceptionEnum.GOODS_ADD_ERROR);
        }
    }

    private void loadCategoryAndBrandName(List<Spu> spus) {
        for (Spu spu : spus) {
            List<String> names = categoryService.queryByIds(Arrays.asList(spu.getCid1(), spu.getCid2(), spu.getCid3()))
                    .stream().map(Category::getName).collect(Collectors.toList());
            spu.setCname(StringUtils.join(names,"/"));
            spu.setBname(brandService.queryById(spu.getBrandId()).getName());
        }
    }
}
