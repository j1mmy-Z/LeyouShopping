package com.leyou.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public enum ExceptionEnum {

    PRICE_CANNOT_BE_NULL(400,"价格不能为空"),
    CATEGORY_NOT_FOUND(404,"商品未查询到"),
    BRAND_NOT_FOUND(404,"品牌未查询到"),
    SPEC_GROUP_NOT_FOUND(404,"品牌规格组未查询到"),
    SPEC_PARAM_NOT_FOUND(404,"品牌规格未查询到"),
    GOODS_NOT_FOUND(404,"商品未查询到"),
    GOODS_SKU_NOT_FOUND(404,"商品SKU未查询到"),
    GOODS_STOCK_NOT_FOUND(404,"商品库存未查询到"),
    GOODS_DETAIL_NOT_FOUND(404,"商品详情未查询到"),
    BRAND_ADD_ERROR(500,"品牌添加失败"),
    GOODS_ADD_ERROR(500,"商品添加失败"),
    GOODS_UPDATE_ERROR(500,"商品更新失败"),
    CATEGORY_BRAND_ADD_ERROR(500,"品牌-分类添加失败"),
    UPLOAD_ERROR(500,"文件上传失败"),
    INVALID_FILE_TYPE(400,"文件格式无效"),
    ID_CANNOT_BE_NULL(500,"ID不能为空"),
    INVALID_USER_DATA_TYPE(400,"无效的用户数据类型"),
    REGISTER_FAILURE(400,"注册失败"),
    LOGIN_FAILURE(400,"登录失败"),
    CARTS_NOT_FOUND(404,"购物车为空"),
    CREATE_ORDER_FAILURE(500,"订单创建失败"),
    CREATE_ORDER_URL_FAILURE(500,"订单支付创建失败"),
    QUERY_ORDER_FAILURE(500,"订单查询失败"),
    STOCK_NOT_ENOUGH(500,"库存不足"),
    INVALID_SIGN(400,"无效的签名"),
    WXPAY_ERROR(400,"微信支付异常"),
    INVALID_ORDER_PARAM(400,"订单参数异常"),
    UPDATE_ORDER_STATUS_ERROR(500,"更新订单状态失败"),
    ;
    private int StatusCode;
    private String msg;
}
