package com.leyou.service;

import com.leyou.domain.Category;


import java.util.List;

public interface CategoryService  {

    public List<Category> queryCategoryListByPid(long pid);

    public List<Category> queryByIds(List<Long> ids);

    List<Category> queryAllByCid3(Long cid3);
}
