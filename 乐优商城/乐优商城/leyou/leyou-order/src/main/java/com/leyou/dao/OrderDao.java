package com.leyou.dao;

import com.leyou.common.dao.BaseDao;
import com.leyou.domain.Order;
import org.apache.ibatis.annotations.Select;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface OrderDao extends BaseDao<Order> {
    @Select("select * from tb_order where user_id = #{userId} and order_id = (select order_id from tb_order_status where status = #{status})")
    List<Order> queryOrderList(@Param("userId") Long userId,@Param("status") Integer status);
}
