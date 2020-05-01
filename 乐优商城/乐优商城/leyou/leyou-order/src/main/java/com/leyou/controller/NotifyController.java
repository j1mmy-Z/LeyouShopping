package com.leyou.controller;

import com.leyou.service.OrderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/notify")
@Slf4j
public class NotifyController {

    @Autowired
    private OrderService orderService;

    /**
     * @Description : 接收微信支付结果通知消息(内网穿透)
     * @param : map接受解析的XML
     * @Return :
     * @Author : Jimmy
     * @Date : 2020/4/30 8:29
     */
    //TODO 方法1：等待微信消息通知，更改订单状态
    @PostMapping(value = "/wxpay",produces = "application/xml")
    public Map<String,String> hello(@RequestBody Map<String,String> result){
        //处理回调
        orderService.handleNotify(result);
        log.info("【微信支付】接受微信通知消息成功！结果：{}",result);
        //返回结果（xml类型）
        Map<String,String> msg=new HashMap<>();
        msg.put("return_code","SUCCESS");
        msg.put("return_msg","OK");
        return msg;
    }
}
