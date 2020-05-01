package com.leyou.search.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.leyou.common.enums.ExceptionEnum;
import com.leyou.common.exception.JimmyException;
import com.leyou.common.utils.JsonUtils;
import com.leyou.common.vo.PageResult;
import com.leyou.domain.*;
import com.leyou.search.client.BrandClient;
import com.leyou.search.client.CategoryClient;
import com.leyou.search.client.GoodsClient;
import com.leyou.search.client.SpecClient;
import com.leyou.search.domain.Goods;
import com.leyou.search.domain.SearchRequest;
import com.leyou.search.domain.SearchResult;
import com.leyou.search.repository.GoodsRepository;
import com.leyou.search.service.SearchService;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.elasticsearch.index.query.*;
import org.elasticsearch.search.aggregations.Aggregation;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.Aggregations;
import org.elasticsearch.search.aggregations.bucket.terms.LongTerms;
import org.elasticsearch.search.aggregations.bucket.terms.StringTerms;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.sort.SortBuilders;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.elasticsearch.core.ElasticsearchTemplate;
import org.springframework.data.elasticsearch.core.aggregation.AggregatedPage;
import org.springframework.data.elasticsearch.core.query.FetchSourceFilter;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class SearchServiceImpl implements SearchService {
    @Autowired
    private CategoryClient categoryClient;
    @Autowired
    private BrandClient brandClient;
    @Autowired
    private GoodsClient goodsClient;
    @Autowired
    private SpecClient specClient;

    @Autowired
    private GoodsRepository goodsRepository;

    @Autowired
    private ElasticsearchTemplate template;

    @Override
    public Goods buildGoods(Spu spu) {
        Long spuId = spu.getId();
        //搜索信息all
            //分类
        List<Category> categories = categoryClient.queryCategoryByIds(
                Arrays.asList(spu.getCid1(), spu.getCid2(), spu.getCid3()));
        if (CollectionUtils.isEmpty(categories)){
            throw new JimmyException(ExceptionEnum.CATEGORY_NOT_FOUND);
        }
        List<String> names = categories.stream().map(Category::getName).collect(Collectors.toList());
            //品牌
        Brand brand = brandClient.queryBrandById(spu.getBrandId());
        if (brand==null){
            throw new JimmyException(ExceptionEnum.BRAND_NOT_FOUND);
        }
        String all=spu.getTitle()+StringUtils.join(names," ")+brand.getName();


        //sku
        List<Sku> skus = goodsClient.querySkuBySpuId(spuId);
        if (CollectionUtils.isEmpty(skus)){
            throw new JimmyException(ExceptionEnum.GOODS_SKU_NOT_FOUND);
        }
        List<Map<String,Object>> skuList=new ArrayList<>();


        //价格
        Set<Long> prices=new HashSet<>();
        for (Sku sku : skus) {
            Map<String,Object> map=new HashMap<>();
            map.put("id",sku.getId());
            map.put("title",sku.getTitle());
            map.put("price",sku.getPrice());
            map.put("image",StringUtils.substringBefore(sku.getImages(),","));
            skuList.add(map);

            prices.add(sku.getPrice());
        }


        //用来过滤查询的规格参数
        List<SpecParam> params = specClient.querySpecParam(null, spu.getCid3(), true);
        if (CollectionUtils.isEmpty(params)){
            throw new JimmyException(ExceptionEnum.SPEC_PARAM_NOT_FOUND);
        }
        SpuDetail spuDetail = goodsClient.querySpuDetailById(spuId);
            //通用
        Map<Long, String> genericSpec = JsonUtils.parseMap(spuDetail.getGenericSpec(), Long.class, String.class);
            //特有
        Map<Long, List<Object>> specialSpec = JsonUtils.
                nativeRead(spuDetail.getSpecialSpec(), new TypeReference<Map<Long, List<Object>>>() {});

        Map<String,Object> specs = new HashMap<>();
        for (SpecParam param : params) {
            String key = param.getName();
            Object value="";
            //通用
            if(param.isGeneric()){
                value=genericSpec.get(param.getId());
                //分段
                if (param.isNumeric()){
                    value = chooseSegment(value.toString(),param);
                }
            //特有
            }else {
                value=specialSpec.get(param.getId());
            }

            specs.put(key,value);
        }

        Goods goods=new Goods();
        goods.setBrandId(spu.getBrandId());
        goods.setCid1(spu.getCid1());
        goods.setCid2(spu.getCid2());
        goods.setCid3(spu.getCid3());
        goods.setCreateTime(spu.getCreateTime());
        goods.setId(spuId);
        goods.setSubTitle(spu.getSubTitle());

        goods.setAll(all);
        goods.setPrice(prices);
        goods.setSkus(JsonUtils.serialize(skuList));
        goods.setSpecs(specs);
        return goods;
    }

    @Override
    public PageResult<Goods> search(SearchRequest searchRequest) {
        String key = searchRequest.getKey();
        if (StringUtils.isBlank(key)){
            return null;
        }
        Integer page = searchRequest.getPage()-1;
        Integer size = searchRequest.getSize();
        String sortBy = searchRequest.getSortBy();
        Boolean descending = searchRequest.getDescending();

        NativeSearchQueryBuilder queryBuilder = new NativeSearchQueryBuilder();
        queryBuilder.withPageable(PageRequest.of(page,size));
        QueryBuilder basicQuery = buildBasicQuery(searchRequest);
        //QueryBuilder basicQuery = QueryBuilders.matchQuery("all", key);
        queryBuilder.withQuery(basicQuery);
        //聚合
        String categoryAggName="category_agg";
        queryBuilder.addAggregation(AggregationBuilders.terms(categoryAggName).field("cid3"));
        String brandAggName="brand_agg";
        queryBuilder.addAggregation(AggregationBuilders.terms(brandAggName).field("brandId"));
        //过滤
        queryBuilder.withSourceFilter(new FetchSourceFilter(new String[]{"id","subTitle","skus"},null));
        if (StringUtils.isNotBlank(sortBy)) {
            queryBuilder.withSort(SortBuilders.fieldSort(sortBy).order(descending ? SortOrder.DESC : SortOrder.ASC));
        }
        AggregatedPage<Goods> result = template.queryForPage(queryBuilder.build(), Goods.class);
        long total = result.getTotalElements();
        long totalPage =  result.getTotalPages();
        List<Goods> goodsList = result.getContent();

        Aggregations aggregations = result.getAggregations();
        List<Category> categories = parseCategoryAgg(aggregations.get(categoryAggName));
        List<Brand> brands = parseBrandAgg(aggregations.get(brandAggName));

        //规格参数聚合
        List<Map<String,Object>> specs=null;
        if (categories.size()==1&&categories!=null){
            specs = buildSpecAgg(categories.get(0).getId(),basicQuery);
        }

        return new SearchResult(total,totalPage,goodsList,categories,brands,specs);
        //return new PageResult<>(total,totalPage,goodsList);
    }

    @Override
    public void creatOrUpdateIndex(Long spuId) {
        Spu spu = goodsClient.querySpuById(spuId);
        Goods goods = buildGoods(spu);
        goodsRepository.save(goods);
    }

    @Override
    public void deleteIndex(Long spuId) {
        goodsRepository.deleteById(spuId);
    }

    private QueryBuilder buildBasicQuery(SearchRequest searchRequest) {
        //bool查询
        BoolQueryBuilder queryBuilder = QueryBuilders.boolQuery();
        queryBuilder.must(QueryBuilders.matchQuery("all",searchRequest.getKey()).operator(Operator.AND));
        BoolQueryBuilder filterQueryBuilder = QueryBuilders.boolQuery();
        Map<String, String> map = searchRequest.getFilter();
            for (Map.Entry<String, String> entry : map.entrySet()) {
                String key = entry.getKey();
                if ("key".equals(key)){
                    break;
                }
                if (!"cid3".equals(key) && !"brandId".equals(key)) {
                    key = "specs." + key + ".keyword";
                }
                String value = entry.getValue();
                filterQueryBuilder.must(QueryBuilders.termQuery(key, value));
            }
            queryBuilder.filter(filterQueryBuilder);

        return queryBuilder;
    }

    private List<Map<String, Object>> buildSpecAgg(Long cid, QueryBuilder basicQuery) {
        List<Map<String,Object>> specs =new ArrayList<>();
        //查询需要聚合的spec
        List<SpecParam> params = specClient.querySpecParam(null, cid, true);
        //聚合
        NativeSearchQueryBuilder queryBuilder = new NativeSearchQueryBuilder();
        queryBuilder.withQuery(basicQuery);
        for (SpecParam param : params) {
            String name = param.getName();
            queryBuilder.addAggregation
                    (AggregationBuilders.terms(name).field("specs."+name+".keyword"));
        }
        AggregatedPage<Goods> result = template.queryForPage(queryBuilder.build(), Goods.class);
        Aggregations aggregations = result.getAggregations();
        for (SpecParam param : params) {
            String name = param.getName();
            StringTerms aggregation = aggregations.get(name);
            List<String> options = aggregation.getBuckets().stream().map(b -> b.getKeyAsString()).collect(Collectors.toList());
            Map<String,Object> map=new HashMap<>();
            map.put("k",name);
            map.put("options",options);
            specs.add(map);
        }
        return specs;
    }

    private List<Brand> parseBrandAgg(LongTerms aggregation) {
        try {
            List<Long> ids = aggregation.getBuckets().stream().
                    map(b -> b.getKeyAsNumber().longValue()).collect(Collectors.toList());
            List<Brand> brands = brandClient.queryBrandByIds(ids);
            return brands;
        }catch (Exception e){
            return null;
        }
    }

    private List<Category> parseCategoryAgg(LongTerms aggregation) {
        try {
            List<Long> ids = aggregation.getBuckets().stream().
                    map(b -> b.getKeyAsNumber().longValue()).collect(Collectors.toList());
            List<Category> categories = categoryClient.queryCategoryByIds(ids);
            return categories;
        } catch (Exception e) {
            return null;
        }
    }


    private String chooseSegment(String value, SpecParam p) {
        double val = NumberUtils.toDouble(value);
        String result = "其它";
        // 保存数值段
        for (String segment : p.getSegments().split(",")) {
            String[] segs = segment.split("-");
            // 获取数值范围
            double begin = NumberUtils.toDouble(segs[0]);
            double end = Double.MAX_VALUE;
            if(segs.length == 2){
                end = NumberUtils.toDouble(segs[1]);
            }
            // 判断是否在范围内
            if(val >= begin && val < end){
                if(segs.length == 1){
                    result = segs[0] + p.getUnit() + "以上";
                }else if(begin == 0){
                    result = segs[1] + p.getUnit() + "以下";
                }else{
                    result = segment + p.getUnit();
                }
                break;
            }
        }
        return result;
    }
}
