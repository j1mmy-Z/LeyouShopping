package com.leyou.service;

import com.leyou.domain.Cart;

import java.util.List;

public interface CartService {
    void addCart(Cart cart);

    List<Cart> queryCarts();

    void updateCartNumber(Cart cart);

    void deleteCart(String skuId);

    void addLocalCarts(List<Cart> carts);

    void emptyCart(Long userId);
}
