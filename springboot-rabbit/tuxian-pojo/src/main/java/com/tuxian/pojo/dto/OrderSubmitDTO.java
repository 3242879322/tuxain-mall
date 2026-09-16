package com.tuxian.pojo.dto;

import lombok.Data;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.List;

/**
 * 提交订单请求参数（对标前端 createOrderAPI 的请求体）。
 */
@Data
public class OrderSubmitDTO implements Serializable {

    /** 配送时间类型 */
    private Integer deliveryTimeType;

    /** 支付方式 */
    private Integer payType;

    /** 支付渠道 */
    private Integer payChannel;

    /** 买家留言 */
    private String buyerMessage;

    /** 要购买的商品列表 */
    @NotEmpty(message = "商品不能为空")
    private List<GoodsDTO> goods;

    /** 收货地址 ID */
    @NotNull(message = "收货地址不能为空")
    private Long addressId;

    /**
     * 订单中的单个商品。
     */
    @Data
    public static class GoodsDTO implements Serializable {
        /** SKU ID */
        private String skuId;
        /** 数量 */
        private Integer count;
    }
}