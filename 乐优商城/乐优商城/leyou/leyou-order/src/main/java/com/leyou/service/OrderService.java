package com.leyou.service;

import com.leyou.common.vo.PageResult;
import com.leyou.domain.Order;

import java.util.Map;

public interface OrderService {

    Long createOrder(Order order);

    Order queryOrderById(Long orderId);

    Boolean updateStatus(Long id, Integer status);

    String createOrderUrl(Long orderId);

    void handleNotify(Map<String, String> result);

    Integer queryOrderStatus(Long orderId);

    PageResult<Order> queryUserOrderList(Integer page, Integer rows, Integer status);
}
