package com.leyou.mq;

import com.leyou.service.CartService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.ExchangeTypes;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class CartListener {

    @Autowired
    private CartService cartService;

    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(name = "leyou.empty.cart.queue",durable = "true"),
            exchange = @Exchange(name = "leyou.cart.exchange",type = ExchangeTypes.TOPIC),
            key = "cart.empty"
    ))
    public void emptyCart(Long userId){
        try {
            cartService.emptyCart(userId);
        }catch (Exception e){
            log.error("【购物车】清空购物车失败，用户id:{}",userId);
        }
    }
}
