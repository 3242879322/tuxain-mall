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
 * 订单项实体，对应表 order_item（下单时的商品/SKU 快照）。
 */
@Data
@TableName("order_item")
public class OrderItem implements Serializable {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 所属订单号 */
    private String orderId;

    /** 商品 ID */
    private String goodsId;

    /** SKU ID */
    private String skuId;

    /** 商品名称快照 */
    private String name;

    /** 商品图片快照 */
    private String picture;

    /** SKU 规格文本快照，如 "颜色:瓷白色 尺寸:8寸" */
    private String attrsText;

    /** 下单时单价 */
    private BigDecimal price;

    /** 下单时实付单价 */
    private BigDecimal payPrice;

    /** 购买数量 */
    private Integer count;

    /** 小计（price * count） */
    private BigDecimal totalPrice;

    /** 实付小计（payPrice * count） */
    private BigDecimal totalPayPrice;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @TableLogic
    @JsonIgnore
    private Integer isDeleted;
}