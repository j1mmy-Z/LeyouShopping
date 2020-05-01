package com.leyou.dao;

import com.leyou.common.dao.BaseDao;
import com.leyou.domain.Brand;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;


import java.util.List;

public interface BrandDao extends BaseDao<Brand> {

    @Insert("insert into tb_category_brand (category_id,brand_id) values(#{cid},#{bid})")
    int addCategoryBrand(@Param("cid") Long cid,@Param("bid") Long bid);

    @Select("SELECT * FROM tb_brand WHERE id IN ( SELECT brand_id FROM tb_category_brand WHERE category_id = #{cid})")
    List<Brand> queryByCid(@Param("cid") Long cid);
}
