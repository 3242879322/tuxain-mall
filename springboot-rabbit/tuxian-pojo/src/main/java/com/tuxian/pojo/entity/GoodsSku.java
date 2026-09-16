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
 * 商品 SKU 实体，对应表 goods_sku。
 * <p>
 * specs 字段保存 JSON 数组字符串，形如：[{"name":"颜色","valueName":"瓷白色"}]
 */
@Data
@TableName("goods_sku")
public class GoodsSku implements Serializable {

    /** SKU ID（字符串） */
    @TableId(type = IdType.INPUT)
    private String id;

    /** 所属商品 ID */
    private String goodsId;

    /** SKU 售价 */
    private BigDecimal price;

    /** SKU 原价 */
    private BigDecimal oldPrice;

    /** 库存数量 */
    private Integer inventory;

    /** 规格组合，JSON 数组字符串 */
    private String specs;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @TableLogic
    @JsonIgnore
    private Integer isDeleted;
}