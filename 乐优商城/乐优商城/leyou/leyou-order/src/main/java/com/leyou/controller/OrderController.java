package com.leyou.controller;
import com.leyou.common.enums.ExceptionEnum;
import com.leyou.common.exception.JimmyException;
import com.leyou.common.vo.PageResult;
import com.leyou.domain.Order;
import com.leyou.service.OrderService;
import io.swagger.annotations.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RestController
@RequestMapping("/order")
@Api("订单服务接口")
public class OrderController {

    @Autowired
    private OrderService orderService;

    @PostMapping
    @ApiOperation(value = "创建订单接口，返回订单编号", notes = "创建订单")
    @ApiImplicitParam(name = "order", required = true, value = "订单的json对象,包含订单条目和物流信息")
    public ResponseEntity<Long> createOrder(@RequestBody @Valid Order order){
        Long orderId = orderService.createOrder(order);
        return ResponseEntity.status(HttpStatus.CREATED).body(orderId);
    }

    @GetMapping("{id}")
    public ResponseEntity<Order> queryOrder(@PathVariable("id") Long orderId){
        Order order = orderService.queryOrderById(orderId);
        if (order==null){
            throw new JimmyException(ExceptionEnum.QUERY_ORDER_FAILURE);
        }
        return ResponseEntity.status(HttpStatus.OK).body(order);
    }

    /**
     * @Description :创建微信支付链接
     * @param orderId
     * @Return : org.springframework.http.ResponseEntity<java.lang.String>
     * @Author : Jimmy
     * @Date : 2020/4/29 15:27
     */
    @GetMapping("/url/{id}")
    public ResponseEntity<String> createOrderUrl(@PathVariable("id") Long orderId){
        String url = orderService.createOrderUrl(orderId);
        if (url==null){
            throw new JimmyException(ExceptionEnum.CREATE_ORDER_URL_FAILURE);
        }
        return ResponseEntity.status(HttpStatus.OK).body(url);
    }


    /**
     * @Description : 查询订单状态
     * @param orderId
     * @Return : Long 订单状态码
     * @Author : Jimmy
     * @Date : 2020/4/30 19:58
     */
    @GetMapping("/state/{id}")
    public ResponseEntity<Integer> queryOrderStatus(@PathVariable("id")Long orderId){
       Integer status = orderService.queryOrderStatus(orderId);
       return ResponseEntity.status(HttpStatus.OK).body(status);
    }

    /**
     * @Description : 分页查询
     * @param
     * @Return :
     * @Author : Jimmy
     */
    @GetMapping("/list")
    @ApiOperation(value = "分页查询当前用户订单，并且可以根据订单状态过滤",
            notes = "分页查询当前用户订单")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "page", value = "当前页",
                    defaultValue = "1", type = "Integer"),
            @ApiImplicitParam(name = "rows", value = "每页大小",
                    defaultValue = "5", type = "Integer"),
            @ApiImplicitParam(
                    name = "status",
                    value = "订单状态：1未付款，2已付款未发货，3已发货未确认，4已确认未评价，5交易关闭，6交易成功，已评价", type = "Integer"),
    })
    @ApiResponses({
            @ApiResponse(code = 200,message = "订单的分页结果"),
            @ApiResponse(code = 404,message = "没有查询到结果"),
            @ApiResponse(code = 500,message = "查询失败")
    })
    public ResponseEntity<PageResult<Order>> queryUserOrderList(
            @RequestParam(value = "page", defaultValue = "1") Integer page,
            @RequestParam(value = "rows", defaultValue = "5") Integer rows,
            @RequestParam(value = "status", required = false) Integer status) {
        PageResult<Order> result = this.orderService.queryUserOrderList(page, rows, status);
        if (result == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        return ResponseEntity.ok(result);
    }
}
