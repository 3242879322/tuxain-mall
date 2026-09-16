package com.tuxian.pojo.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 订单详情 VO（/member/order/{id} 返回，支付页展示用）。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderDetailVO implements Serializable {

    private String id;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    /** 订单状态：1~6 */
    private Integer orderState;

    /** 付款剩余秒数 */
    private Long countdown;

    /** 应付总额 */
    private BigDecimal payMoney;

    /** 商品总价 */
    private BigDecimal totalMoney;

    /** 邮费 */
    private BigDecimal postFee;

    /** 支付方式 */
    private Integer payType;

    /** 配送时间类型 */
    private Integer deliveryTimeType;

    /** 买家留言 */
    private String buyerMessage;

    /** 收货人（地址快照） */
    private String receiver;

    /** 联系方式（地址快照） */
    private String contact;

    /** 省市区（地址快照） */
    private String fullLocation;

    /** 详细地址（地址快照） */
    private String address;

    /** 订单商品列表 */
    private List<OrderItemVO> skus;
}