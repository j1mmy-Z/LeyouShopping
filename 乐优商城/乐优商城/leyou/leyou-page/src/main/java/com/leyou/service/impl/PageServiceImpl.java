package com.leyou.service.impl;

import com.leyou.client.BrandClient;
import com.leyou.client.CategoryClient;
import com.leyou.client.GoodsClient;
import com.leyou.client.SpecClient;
import com.leyou.domain.*;
import com.leyou.service.PageService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class PageServiceImpl implements PageService {

    @Autowired
    private BrandClient brandClient;
    @Autowired
    private CategoryClient categoryClient;
    @Autowired
    private GoodsClient goodsClient;
    @Autowired
    private SpecClient specClient;
    @Autowired
    private TemplateEngine templateEngine;
    @Override
    public Map<String, Object> loadModel(Long spuId) {
        Map<String,Object> model = new HashMap<>();
        Spu spu = goodsClient.querySpuById(spuId);
        List<Sku> skus = spu.getSkus();
        SpuDetail spuDetail = spu.getSpuDetail();
        Brand brand = brandClient.queryBrandById(spu.getBrandId());
        List<Category> categories = categoryClient.queryCategoryByIds(Arrays.asList(spu.getCid1(), spu.getCid2(), spu.getCid3()));
        List<SpecGroup> specs = specClient.queryListByCid(spu.getCid3());

        model.put("title", spu.getTitle());
        model.put("subTitle", spu.getSubTitle());
        model.put("detail", spuDetail);
        model.put("skus", skus);
        model.put("brand", brand);
        model.put("categories", categories);
        model.put("specs",specs);
        return model;
    }

    //html静态化方法
    public void createHtml(Long spuId){
        //上下文context
        Context context = new Context();
        context.setVariables(loadModel(spuId));
        //输出流
        File dest = new File("D:\\upload", spuId + ".html");
        if (dest.exists()){
            dest.delete();
        }
        try(PrintWriter printWriter = new PrintWriter(dest,"UTF-8")){
            //生成静态html
            templateEngine.process("item", context,printWriter);
        }catch (Exception e){
            log.error("【静态页服务】生成静态页异常",e);
        }
    }

    @Override
    public void deleteHtml(Long spuId) {
        File dest = new File("D:\\upload", spuId + ".html");
        if (dest.exists()){
            dest.delete();
        }
    }
}
