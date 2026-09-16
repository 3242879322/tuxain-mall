package com.tuxian.pojo.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.io.Serializable;

/**
 * 加入购物车请求参数。
 */
@Data
public class CartItemDTO implements Serializable {

    /** SKU ID */
    @NotBlank(message = "skuId 不能为空")
    private String skuId;

    /** 数量 */
    @NotNull(message = "count 不能为空")
    private Integer count;
}