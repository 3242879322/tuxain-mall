package com.tuxian.pojo.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 订单列表/详情中的订单项 VO。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderItemVO implements Serializable {

    /** 订单项 ID */
    private Long id;

    /** 商品图片 */
    private String image;

    /** 商品名称 */
    private String name;

    /** 规格文本 */
    private String attrsText;

    /** 实付单价 */
    private BigDecimal realPay;

    /** 数量 */
    private Integer quantity;
}