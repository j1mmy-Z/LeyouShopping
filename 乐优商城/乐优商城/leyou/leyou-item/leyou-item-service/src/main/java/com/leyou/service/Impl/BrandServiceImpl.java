package com.leyou.service.Impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.leyou.common.enums.ExceptionEnum;
import com.leyou.common.exception.JimmyException;
import com.leyou.common.vo.PageResult;
import com.leyou.dao.BrandDao;
import com.leyou.domain.Brand;
import com.leyou.service.BrandService;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import tk.mybatis.mapper.entity.Example;

import java.util.List;

@Service
public class BrandServiceImpl implements BrandService {
    @Autowired
    private BrandDao brandDao;

    @Override
    public PageResult<Brand> queryBrandByPage(Integer page, Integer rows, String sortBy, boolean desc, String key) {
        PageHelper.startPage(page,rows);//分页
        Example example = new Example(Brand.class);
        if (StringUtils.isNotBlank(key)){
            example.createCriteria().orLike("name","%"+key+"%").orEqualTo("letter",key.toUpperCase());
        }
        if (StringUtils.isNotBlank(sortBy)){
            example.setOrderByClause(sortBy+(desc ? " DESC" : " ASC"));
        }
        List<Brand> list = brandDao.selectByExample(example);
        if (CollectionUtils.isEmpty(list)){
            throw new JimmyException(ExceptionEnum.BRAND_NOT_FOUND);
        }
        PageInfo<Brand> pageInfo = new PageInfo<>(list);
        return new PageResult<>(pageInfo.getTotal(),list);
    }

    @Override
    @Transactional
    public void addBrand(Brand brand, List<Long> cids) {
        brand.setId(null);
        int count = brandDao.insert(brand);
        if (count!=1){
            throw new JimmyException(ExceptionEnum.BRAND_ADD_ERROR);
        }
        for (Long cid : cids) {
            count = brandDao.addCategoryBrand(cid, brand.getId());
            if (count!=1){
                throw new JimmyException(ExceptionEnum.CATEGORY_BRAND_ADD_ERROR);
            }
        }
    }

    @Override
    public Brand queryById(Long id) {
        Brand brand = brandDao.selectByPrimaryKey(id);
        if (brand==null){
            throw new JimmyException(ExceptionEnum.BRAND_NOT_FOUND);
        }
        return brand;
    }

    @Override
    public List<Brand> queryBrandByCid(Long cid) {
        List<Brand> brands = brandDao.queryByCid(cid);
        if (CollectionUtils.isEmpty(brands)){
            throw new JimmyException(ExceptionEnum.BRAND_NOT_FOUND);
        }
        return brands;
    }

    @Override
    public List<Brand> queryByIds(List<Long> ids) {
        List<Brand> brands = brandDao.selectByIdList(ids);
        if (CollectionUtils.isEmpty(brands)){
            throw new JimmyException(ExceptionEnum.BRAND_NOT_FOUND);
        }
        return brands;
    }
}
