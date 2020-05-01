package com.leyou.service;

import com.leyou.common.vo.PageResult;
import com.leyou.domain.Brand;

import java.util.List;

public interface BrandService {
    PageResult<Brand> queryBrandByPage(Integer page, Integer rows, String sortBy, boolean desc, String key);

    void addBrand(Brand brand, List<Long> cids);

    Brand queryById(Long id);

    List<Brand> queryBrandByCid(Long cid);

    List<Brand> queryByIds(List<Long> ids);
}
