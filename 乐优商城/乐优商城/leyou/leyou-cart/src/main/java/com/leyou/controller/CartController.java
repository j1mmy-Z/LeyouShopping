package com.leyou.controller;

import com.leyou.common.enums.ExceptionEnum;
import com.leyou.common.exception.JimmyException;
import com.leyou.domain.Cart;
import com.leyou.service.CartService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.*;

import java.util.Collection;
import java.util.List;
import java.util.Map;

@RestController
public class CartController {
    @Autowired
    private CartService cartService;

    @PostMapping
    public ResponseEntity<Void> addCart(@RequestBody Cart cart){
        cartService.addCart(cart);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @GetMapping
    public ResponseEntity<List<Cart>> queryCarts(){
        List<Cart> carts = cartService.queryCarts();
        if (CollectionUtils.isEmpty(carts)){
            throw new JimmyException(ExceptionEnum.CARTS_NOT_FOUND);
        }
        return ResponseEntity.status(HttpStatus.OK).body(carts);
    }

    @PutMapping
    public ResponseEntity<Void> updateCartNumber(@RequestBody Cart cart){
        cartService.updateCartNumber(cart);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @DeleteMapping("{skuId}")
    public ResponseEntity<Void> deleteCart(@PathVariable("skuId") String skuId){
        cartService.deleteCart(skuId);
        return ResponseEntity.status(HttpStatus.OK).build();
    }

    @PostMapping("/addLocal")
    public ResponseEntity<Void> addLocalCarts(@RequestBody List<Cart> carts){
        cartService.addLocalCarts(carts);
        return ResponseEntity.status(HttpStatus.OK).build();
    }

}
