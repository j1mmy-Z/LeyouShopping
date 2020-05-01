package com.leyou.dao;


import com.leyou.common.dao.BaseDao;
import com.leyou.domain.Stock;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;


public interface StockDao extends BaseDao<Stock> {

    @Update("update tb_stock set stock=stock - #{num} where sku_id=#{skuId} and stock>= #{num}")
    int decreaseStock(@Param("skuId")Long skuId,@Param("num")Integer num);
}
