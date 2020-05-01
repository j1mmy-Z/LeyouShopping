package com.leyou.utils;

import com.github.wxpay.sdk.WXPay;
import static com.github.wxpay.sdk.WXPayConstants.*;

import com.github.wxpay.sdk.WXPayUtil;
import com.leyou.common.enums.ExceptionEnum;
import com.leyou.common.exception.JimmyException;
import com.leyou.config.PayProperties;
import com.leyou.enums.OrderStatusEnum;
import com.leyou.service.OrderService;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Component
public class PayHelper {

    @Autowired
    private WXPay wxPay;

    private static final Logger logger = LoggerFactory.getLogger(PayHelper.class);

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Autowired
    private OrderService orderService;

    @Autowired
    private PayProperties payProperties;


    public String createPayUrl(Long orderId,Long totalFee,String desc) {
        String key = "ly.pay.url." + orderId;
        try {
            String url = this.redisTemplate.opsForValue().get(key);
            if (StringUtils.isNotBlank(url)) {
                return url;
            }
        } catch (Exception e) {
            logger.error("查询缓存付款链接异常,订单编号：{}", orderId, e);
        }

        try {
            Map<String, String> data = new HashMap<>();
            // 商品描述
            data.put("body", desc);
            // 订单号
            data.put("out_trade_no", orderId.toString());
            //货币
            data.put("fee_type", "CNY");
            //金额，单位是分
            data.put("total_fee", totalFee.toString());
            //调用微信支付的终端IP（乐优商城的IP）
            data.put("spbill_create_ip", "127.0.0.1");
            //回调地址，付款成功后的接口
            data.put("notify_url", "http://54fr5n.natappfree.cc/notify/wxpay");
            // 交易类型为扫码支付
            data.put("trade_type", "NATIVE");
            //商品id,使用假数据
            data.put("product_id", "1234567");

            //利用wxPay发送订单给wx
            Map<String, String> result = this.wxPay.unifiedOrder(data);
            //

            //判断通信是否成功
            if (SUCCESS.equals(result.get("return_code"))) {
                //判断创建订单是否成功
                if (FAIL.equals(result.get("result_code"))){
                    logger.error("创建订单失败,错误信息：{}", result.get("err_code_des"));
                    return null;
                }
                String url = result.get("code_url");
                // 将付款地址缓存，时间为10分钟
                try {
                    this.redisTemplate.opsForValue().set(key, url, 10, TimeUnit.MINUTES);
                } catch (Exception e) {
                    logger.error("缓存付款链接异常,订单编号：{}", orderId, e);
                }
                return url;
            } else {
                logger.error("创建预交易订单通信失败，错误信息：{}", result.get("return_msg"));
                return null;
            }
        } catch (Exception e) {
            logger.error("创建预交易订单异常", e);
            return null;
        }
    }

    /**
     * 查询订单状态
     *
     * @param orderId
     * @return
     */
    //TODO 方法2：主动查询订单状态
    public PayState queryOrder(Long orderId) {
        Map<String, String> data = new HashMap<>();
        // 订单号
        data.put("out_trade_no", orderId.toString());
        try {
            Map<String, String> result = this.wxPay.orderQuery(data);
            if (result == null) {
                // 未查询到结果，认为是未付款
                return PayState.NOT_PAY;
            }
            //TODO 校验签名、付款金额
            String state = result.get("trade_state");
            if ("SUCCESS".equals(state)) {
                // success，则认为付款成功

                // 修改订单状态
                this.orderService.updateStatus(orderId, OrderStatusEnum.PAYED.value());
                logger.info("【微信支付】订单支付成功！订单编号：{}",orderId);
                return PayState.SUCCESS;
            } else if (StringUtils.equals("USERPAYING", state) || StringUtils.equals("NOTPAY", state)) {
                // 未付款或正在付款，都认为是未付款
                return PayState.NOT_PAY;
            } else {
                // 其它状态认为是付款失败
                return PayState.FAIL;
            }
        } catch (Exception e) {
            logger.error("查询订单状态异常", e);
            return PayState.NOT_PAY;
        }
    }

    /**
     * @Description : 判断微信通知结果是否成功
     * @param result
     * @Return : void
     * @Author : Jimmy
     * @Date : 2020/4/30 19:31
     */
    public void isSuccess(Map<String, String> result){
        if (FAIL.equals(result.get("return_code"))){
            logger.error("【微信支付】微信消息通信异常！错误消息:{}", result.get("return_msg"));
            throw new JimmyException(ExceptionEnum.WXPAY_ERROR);
        }
        if (FAIL.equals(result.get("result_code"))){
            logger.error("【微信支付】微信消息结果异常！错误消息:{}", result.get("err_code_des"));
            throw new JimmyException(ExceptionEnum.WXPAY_ERROR);
        }
    }

    /**
     * @Description : 判断签名是否有效
     * @param result
     * @Return : void
     * @Author : Jimmy
     * @Date : 2020/4/30 19:32
     */
    public void isSignValid(Map<String, String> result){
        try {
            //重新生成签名
            String sign1 = WXPayUtil.generateSignature(result, payProperties.getKey(), SignType.MD5);
            String sign2 = WXPayUtil.generateSignature(result, payProperties.getKey(), SignType.HMACSHA256);
            //校验签名
            String sign = result.get("sign");
            if ( !StringUtils.equals(sign1,sign) && !StringUtils.equals(sign2,sign) ){
                throw new JimmyException(ExceptionEnum.INVALID_SIGN);
            }
        } catch (Exception e) {
            logger.error("【微信支付】签名验证失败！");
            throw new JimmyException(ExceptionEnum.INVALID_SIGN);
        }
    }
}
