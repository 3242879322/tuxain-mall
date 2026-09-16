package com.tuxian.pojo.vo;

import com.tuxian.pojo.entity.UserAddress;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

/**
 * 订单预结算 VO（/member/order/pre 返回）。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderPreVO implements Serializable {

    /** 待结算商品列表 */
    private List<GoodsVO> goods;

    /** 金额汇总 */
    private SummaryVO summary;

    /** 收货地址列表 */
    private List<UserAddress> userAddresses;

    /**
     * 待结算商品。
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class GoodsVO implements Serializable {
        private String id;
        private String skuId;
        private String name;
        private String picture;
        /** 原单价 */
        private BigDecimal price;
        /** 实付单价 */
        private BigDecimal payPrice;
        /** 数量 */
        private Integer count;
        /** 规格文本 */
        private String attrsText;
        /** 小计（price * count） */
        private BigDecimal totalPrice;
        /** 实付小计（payPrice * count） */
        private BigDecimal totalPayPrice;
    }

    /**
     * 金额汇总。
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SummaryVO implements Serializable {
        /** 商品件数 */
        private Integer goodsCount;
        /** 商品总价 */
        private BigDecimal totalPrice;
        /** 邮费 */
        private BigDecimal postFee;
        /** 应付总额 */
        private BigDecimal totalPayPrice;
    }
}