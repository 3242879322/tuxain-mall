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
 * 订单列表项 VO（/member/order 返回 items 数组元素）。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderVO implements Serializable {

    /** 订单号 */
    private String id;

    /** 下单时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    /** 订单状态：1~6 */
    private Integer orderState;

    /** 付款剩余时间，格式 HH:mm:ss（仅待付款订单有值，其余为 null） */
    private String countdown;

    /** 订单商品列表 */
    private List<OrderItemVO> skus;

    /** 应付总额 */
    private BigDecimal payMoney;

    /** 邮费 */
    private BigDecimal postFee;

    /** 商品总价 */
    private BigDecimal totalMoney;
}