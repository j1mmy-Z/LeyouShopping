package com.leyou.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public enum OrderStatusEnum {

    NON_PAYMENT(1,"未付款"),
    PAYED(2,"已付款，未发货"),
    DELIVERED(3,"已发货，未确认"),
    SUCCEED(4,"交易成功"),
    CLOSED(5,"交易关闭"),
    COMMENTED(6,"已评价"),
    ;
    private int statusCode;
    private String description;

    public int value(){
        return this.statusCode;
    }
}
