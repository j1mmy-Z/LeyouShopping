package com.leyou.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.leyou.client.GoodsClient;
import com.leyou.common.dto.CartDTO;
import com.leyou.common.enums.ExceptionEnum;
import com.leyou.common.exception.JimmyException;
import com.leyou.common.utils.IdWorker;
import com.leyou.common.vo.PageResult;
import com.leyou.dao.OrderDao;
import com.leyou.dao.OrderDetailDao;
import com.leyou.dao.OrderStatusDao;
import com.leyou.domain.Order;
import com.leyou.domain.OrderDetail;
import com.leyou.domain.OrderStatus;
import com.leyou.domain.UserInfo;
import com.leyou.enums.OrderStatusEnum;
import com.leyou.interceptor.LoginInterceptor;
import com.leyou.service.OrderService;
import com.leyou.utils.PayHelper;
import com.leyou.utils.PayState;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class OrderServiceImpl implements OrderService {
    @Autowired
    private PayHelper payHelper;
    @Autowired
    private OrderDao orderDao;
    @Autowired
    private OrderDetailDao orderDetailDao;
    @Autowired
    private OrderStatusDao orderStatusDao;
    @Autowired
    private GoodsClient goodsClient;

    @Autowired
    private IdWorker idWorker;

    @Autowired
    private AmqpTemplate amqpTemplate;

    @Override
    @Transactional
    public Long createOrder(Order order) {
        //TODO 收货人信息和快递信息为假信息 将来可以拓展业务用feign去查询
        //生成订单编号
        Long orderId = idWorker.nextId();
        order.setOrderId(orderId);
        //获取用户信息
        Long userId = LoginInterceptor.getUserInfo().getId();
        order.setUserId(userId);
        order.setCreateTime(new Date());
        //保存order
        int count = orderDao.insertSelective(order);
        if (count!=1){
            log.error("【创建订单】创建订单失败，订单号：{}",orderId);
            throw new JimmyException(ExceptionEnum.CREATE_ORDER_FAILURE);
        }

        //保存orderStatus
        OrderStatus orderStatus = new OrderStatus();
        orderStatus.setOrderId(orderId);
        orderStatus.setCreateTime(order.getCreateTime());
        orderStatus.setStatus(OrderStatusEnum.NON_PAYMENT.value());
        count  = orderStatusDao.insertSelective(orderStatus);
        if (count!=1){
            log.error("【创建订单】创建订单失败，订单号：{}",orderId);
            throw new JimmyException(ExceptionEnum.CREATE_ORDER_FAILURE);
        }

        //保存orderDetail
        order.getOrderDetails().forEach(d->d.setOrderId(orderId));
        count = orderDetailDao.insertList(order.getOrderDetails());
        if (count!=order.getOrderDetails().size()){
            log.error("【创建订单】创建订单失败，订单号：{}",orderId);
            throw new JimmyException(ExceptionEnum.CREATE_ORDER_FAILURE);
        }
        log.info("生成订单，订单号：{},用户id：{}",orderId,userId);

        //减库存
        List<CartDTO> cartDTOS=new ArrayList<>();
        for (OrderDetail orderDetail : order.getOrderDetails()) {
            CartDTO cartDTO = new CartDTO(orderDetail.getSkuId(), orderDetail.getNum());
            cartDTOS.add(cartDTO);
        }
        goodsClient.decreaseStock(cartDTOS);

        //订单创建完毕清空购物车
        amqpTemplate.convertAndSend("leyou.cart.exchange","cart.empty",userId);
        return orderId;
    }

    @Override
    @Transactional
    public Order queryOrderById(Long orderId) {
        //查询订单
        Order order = orderDao.selectByPrimaryKey(orderId);
        if (order==null){
            return null;
        }
        //查询订单详情
        OrderDetail record = new OrderDetail();
        record.setOrderId(orderId);
        List<OrderDetail> orderDetails = orderDetailDao.select(record);
        if (CollectionUtils.isEmpty(orderDetails)){
            return null;
        }
        order.setOrderDetails(orderDetails);
        //查询订单状态
        OrderStatus orderStatus = orderStatusDao.selectByPrimaryKey(orderId);
        if (orderStatus==null){
            return null;
        }
        order.setOrderStatus(orderStatus);
        return order;
    }

    @Transactional
    public Boolean updateStatus(Long id, Integer status) {
        OrderStatus record = new OrderStatus();
        record.setOrderId(id);
        record.setStatus(status);
        // 根据状态判断要修改的时间
        switch (status) {
            case 2:
                record.setPaymentTime(new Date());// 付款
                break;
            case 3:
                record.setConsignTime(new Date());// 发货
                break;
            case 4:
                record.setEndTime(new Date());// 确认收获，订单结束
                break;
            case 5:
                record.setCloseTime(new Date());// 交易失败，订单关闭
                break;
            case 6:
                record.setCommentTime(new Date());// 评价时间
                break;
            default:
                return null;
        }
        int count = orderStatusDao.updateByPrimaryKeySelective(record);
        return count == 1;
    }

    @Override
    public String createOrderUrl(Long orderId) {
        //查询订单
        Order order = queryOrderById(orderId);
        if (order.getOrderStatus().getStatus()!=OrderStatusEnum.NON_PAYMENT.value()){
            //不是未付款状态
            log.error("【创建订单】订单状态无效，订单id：{}",orderId);
            return null;
        }
        @NotNull Long actualPay = order.getActualPay();
        OrderDetail orderDetail = order.getOrderDetails().get(0);
        String desc = orderDetail.getTitle();
        //TODO 创建订单时传id，金额，描述
        String url = payHelper.createPayUrl(orderId,1L,desc);
        return url;
    }

    /**
     * @Description : 处理微信消息通知
     * @param result
     * @Return : void
     * @Author : Jimmy
     * @Date : 2020/4/30 14:43
     */
    @Override
    public void handleNotify(Map<String, String> result) {
        //数据校验
        payHelper.isSuccess(result);
        //校验签名
        payHelper.isSignValid(result);
        //校验金额
        String totalFeeStr = result.get("total_fee");
        String tradeNo = result.get("out_trade_no");
        if (StringUtils.isEmpty(totalFeeStr)||StringUtils.isEmpty(tradeNo)){
            throw new JimmyException(ExceptionEnum.INVALID_ORDER_PARAM);
        }
        Long totalFee=Long.valueOf(totalFeeStr);
        Long orderId = Long.valueOf(tradeNo);
        Order order = orderDao.selectByPrimaryKey(orderId);
        //TODO 校验真实金额
        if (totalFee != /*order.getActualPay()*/ 1L){
            throw new JimmyException(ExceptionEnum.INVALID_ORDER_PARAM);
        }

        //修改订单状态
        OrderStatus orderStatus = new OrderStatus();
        Boolean flag = updateStatus(orderId, OrderStatusEnum.PAYED.value());
        if (flag==false){
            throw new JimmyException(ExceptionEnum.UPDATE_ORDER_STATUS_ERROR);
        }
        log.info("【微信支付】订单支付成功！订单编号：{}",orderId);
    }

    /**
     * @Description : 查询订单状态
     * @param orderId
     * @Return : 0未支付 1已付款 2失败
     * @Author : Jimmy
     * @Date : 2020/4/30 20:06
     */
    @Override
    public Integer queryOrderStatus(Long orderId) {
        //查询数据库，若已支付，说明微信消息接收成功，直接返回
        OrderStatus orderStatus = orderStatusDao.selectByPrimaryKey(orderId);
        Integer status = orderStatus.getStatus();
        if (status!=OrderStatusEnum.NON_PAYMENT.value()){
            return PayState.SUCCESS.getValue();
        }
        //若数据库查询结果为未支付，可能是微信通知接收失败，再发起主动查询
        PayState payState = payHelper.queryOrder(orderId);
        return payState.getValue();
    }


    public PageResult<Order> queryUserOrderList(Integer page, Integer rows, Integer status) {
        try {
            // 分页
            PageHelper.startPage(page, rows);
            // 获取登录用户
            UserInfo user = LoginInterceptor.getUserInfo();
            // 查询
            Page<Order> pageInfo = (Page<Order>) this.orderDao.queryOrderList(user.getId(), status);
            return new PageResult<>(pageInfo.getTotal(), pageInfo);
        } catch (Exception e) {
            log.error("查询订单出错", e);
            return null;
        }
    }
}
