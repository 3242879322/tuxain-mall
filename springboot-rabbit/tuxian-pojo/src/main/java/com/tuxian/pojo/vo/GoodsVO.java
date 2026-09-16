package com.tuxian.pojo.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * 商品简要信息 VO（用于首页"新鲜好物"、分类商品列表、热榜等列表场景）。
 * <p>
 * 前端接口约定 price 为字符串（如 "646.00"），故此处 price 为 String。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GoodsVO implements Serializable {

    private String id;
    private String name;
    private String desc;
    private String price;
    private String picture;

    /**
     * 把价格格式化为两位小数字符串。
     */
    public static String formatPrice(BigDecimal price) {
        if (price == null) {
            return null;
        }
        return price.setScale(2, RoundingMode.HALF_UP).toPlainString();
    }
}