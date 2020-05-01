package com.leyou.service.Impl;

import com.leyou.common.enums.ExceptionEnum;
import com.leyou.common.exception.JimmyException;
import com.leyou.dao.CategoryDao;
import com.leyou.domain.Category;
import com.leyou.service.CategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;

@Service
public class CategoryServiceImpl implements CategoryService {
    @Autowired
    private CategoryDao categoryDao;

    @Override
    public List<Category> queryCategoryListByPid(long pid) {
        Category category = new Category();
        category.setParentId(pid);
        List<Category> categoryList = categoryDao.select(category);
        if (CollectionUtils.isEmpty(categoryList)){
            throw new JimmyException(ExceptionEnum.CATEGORY_NOT_FOUND);
        }
        return categoryList;
    }

    public List<Category> queryByIds(List<Long> ids){
        List<Category> categories = categoryDao.selectByIdList(ids);
        if (CollectionUtils.isEmpty(categories)){
            throw new JimmyException(ExceptionEnum.CATEGORY_NOT_FOUND);
        }
        return categories;
    }

    @Override
    public List<Category> queryAllByCid3(Long cid3) {
        Category category3 = categoryDao.selectByPrimaryKey(cid3);
        Category category2=null;
        if (category3.getParentId()!=0){
        category2 = categoryDao.selectByPrimaryKey(category3.getParentId());
        }
        Category category1=null;
        if (category2.getParentId()!=0){
        category1 = categoryDao.selectByPrimaryKey(category2.getParentId());
        }
        List<Category> categories = Arrays.asList(category1, category2, category3);
        if (CollectionUtils.isEmpty(categories)){
            throw new JimmyException(ExceptionEnum.CATEGORY_NOT_FOUND);
        }
        return categories;
    }
}
