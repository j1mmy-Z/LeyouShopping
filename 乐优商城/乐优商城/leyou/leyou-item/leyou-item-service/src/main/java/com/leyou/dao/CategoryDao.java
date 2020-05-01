package com.leyou.dao;

import com.leyou.domain.Category;
import tk.mybatis.mapper.additional.idlist.IdListMapper;
import tk.mybatis.mapper.common.Mapper;

public interface CategoryDao extends Mapper<Category>, IdListMapper<Category,Long> {

}