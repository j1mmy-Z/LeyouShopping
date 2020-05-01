package com.leyou.service.impl;

import com.leyou.common.enums.ExceptionEnum;
import com.leyou.common.exception.JimmyException;
import com.leyou.domain.Cart;
import com.leyou.interceptor.LoginInterceptor;
import com.leyou.common.utils.JsonUtils;
import com.leyou.domain.UserInfo;
import com.leyou.service.CartService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.BoundHashOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class CartServiceImpl implements CartService {
    @Autowired
    private StringRedisTemplate redisTemplate;

    private static final String KEY_PREFIX= "cart:userId:";
    @Override
    public void addCart(Cart cart) {
        //获取登录用户
        UserInfo user = LoginInterceptor.getUserInfo();
        String key = KEY_PREFIX+user.getId();
        String hashKey = cart.getSkuId().toString();

        Integer num = cart.getNum();

        BoundHashOperations<String, Object, Object> operation = redisTemplate.boundHashOps(key);
        //购物车商品是否存在
        if (operation.hasKey(hashKey)){
            String json = operation.get(hashKey).toString();
            cart = JsonUtils.parse(json, cart.getClass());
            cart.setNum(num+cart.getNum());
        }
        operation.put(hashKey,JsonUtils.serialize(cart));
    }

    @Override
    public List<Cart> queryCarts() {
        Long userId = LoginInterceptor.getUserInfo().getId();
        String key=KEY_PREFIX+userId;
        if (!redisTemplate.hasKey(key)){
            return null;
        }
        BoundHashOperations<String, Object, Object> operations = redisTemplate.boundHashOps(key);
        List<Object> cartsJson = operations.values();
        if (CollectionUtils.isEmpty(cartsJson)){
            return null;
        }
        return cartsJson.stream().map(c -> JsonUtils.parse(c.toString(), Cart.class)).collect(Collectors.toList());
    }

    @Override
    public void updateCartNumber(Cart cart) {
        Long userId = LoginInterceptor.getUserInfo().getId();
        String key=KEY_PREFIX+userId;
        if (!redisTemplate.hasKey(key)){
            return;
        }
        Integer num = cart.getNum();
        BoundHashOperations<String, Object, Object> operations = redisTemplate.boundHashOps(key);
        String cartJson = operations.get(cart.getSkuId().toString()).toString();
        cart = JsonUtils.parse(cartJson, Cart.class);
        cart.setNum(num);
        operations.put(cart.getSkuId().toString(),JsonUtils.serialize(cart));
    }

    @Override
    public void deleteCart(String skuId) {
        Long userId = LoginInterceptor.getUserInfo().getId();
        String key=KEY_PREFIX+userId;
        if (!redisTemplate.hasKey(key)){
            return;
        }
        BoundHashOperations<String, Object, Object> operations = redisTemplate.boundHashOps(key);
        operations.delete(skuId);
    }

    @Override
    public void addLocalCarts(List<Cart> carts) {
        for (Cart cart : carts) {
            addCart(cart);
        }
    }

    @Override
    public void emptyCart(Long userId) {
        String key=KEY_PREFIX+userId;
        try {
           redisTemplate.delete(key);
        }catch (Exception e){
            throw new JimmyException(ExceptionEnum.CREATE_ORDER_FAILURE);
        }
    }
}
