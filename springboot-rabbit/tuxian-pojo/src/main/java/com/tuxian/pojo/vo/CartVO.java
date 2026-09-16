package com.tuxian.pojo.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 购物车项 VO。
 * <p>
 * 前端购物车列表使用字段：id、skuId、name、picture、price、count、attrsText、selected。
 * 其中 price 参与数字运算（price * count），故使用 BigDecimal 序列化为数字。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CartVO implements Serializable {

    /** 购物车项 ID（以 skuId 作为唯一标识） */
    private String id;

    /** SKU ID */
    private String skuId;

    /** 商品名称 */
    private String name;

    /** 商品图片 */
    private String picture;

    /** 单价 */
    private BigDecimal price;

    /** 数量 */
    private Integer count;

    /** 规格文本，如 "颜色:瓷白色 尺寸:8寸" */
    private String attrsText;

    /** 是否选中 */
    private Boolean selected;
}