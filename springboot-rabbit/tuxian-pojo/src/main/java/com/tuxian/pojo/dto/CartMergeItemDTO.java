package com.tuxian.pojo.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * 合并购物车（登录后把本地购物车合并到服务端）的单个条目。
 */
@Data
public class CartMergeItemDTO implements Serializable {

    /** SKU ID */
    private String skuId;

    /** 是否选中 */
    private Boolean selected;

    /** 数量 */
    private Integer count;
}