package com.tuxian.pojo.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 订单实体，对应表 orders。
 */
@Data
@TableName("orders")
public class Orders implements Serializable {

    /** 订单号（字符串，形如时间戳+随机数） */
    @TableId(type = IdType.INPUT)
    private String id;

    /** 下单用户 ID */
    private Long userId;

    /** 订单状态：1 待付款 2 待发货 3 待收货 4 待评价 5 已完成 6 已取消 */
    private Integer orderState;

    /** 商品总价 */
    private BigDecimal totalMoney;

    /** 邮费 */
    private BigDecimal postFee;

    /** 应付总额（totalMoney + postFee - 优惠） */
    private BigDecimal payMoney;

    /** 支付方式 */
    private Integer payType;

    /** 支付渠道 */
    private Integer payChannel;

    /** 配送时间类型 */
    private Integer deliveryTimeType;

    /** 买家留言 */
    private String buyerMessage;

    /** 收货地址快照，JSON 字符串 */
    private String addressSnapshot;

    /** 支付时间 */
    private LocalDateTime payTime;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @TableLogic
    @JsonIgnore
    private Integer isDeleted;
}