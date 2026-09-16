package com.tuxian.pojo.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

/**
 * 商品详情 VO（/goods?id=xxx 返回）。
 * <p>
 * 字段严格对齐前端 Detail/index.vue 与 XtxSku 组件：
 * specs（规格定义）+ skus（规格组合）+ mainPictures + details + brand 等。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GoodsDetailVO implements Serializable {

    private String id;
    private String name;
    private String desc;
    private BigDecimal price;
    private BigDecimal oldPrice;

    /** 主图列表 */
    private List<String> mainPictures;

    /** 面包屑分类：[0] 二级分类 [1] 一级分类 */
    private List<CategoryVO> categories;

    /** 规格定义 */
    private List<SpecVO> specs;

    /** SKU 组合 */
    private List<SkuVO> skus;

    /** 详情（属性 + 图片） */
    private DetailVO details;

    /** 品牌 */
    private BrandVO brand;

    private Integer salesCount;
    private Integer commentCount;
    private Integer collectCount;

    /**
     * 分类简要信息。
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CategoryVO implements Serializable {
        private String id;
        private String name;
    }

    /**
     * 规格定义（如"颜色"），包含所有可选值。
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SpecVO implements Serializable {
        private String id;
        private String name;
        private List<SpecValueVO> values;
    }

    /**
     * 规格可选值。
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SpecValueVO implements Serializable {
        private String name;
        private String picture;
    }

    /**
     * SKU 组合。
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SkuVO implements Serializable {
        private String id;
        private BigDecimal price;
        private BigDecimal oldPrice;
        private Integer inventory;
        /** 该 SKU 对应的规格组合，如 [{name:"颜色",valueName:"瓷白色"}] */
        private List<SkuSpecVO> specs;
    }

    /**
     * SKU 的单个规格值。
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SkuSpecVO implements Serializable {
        private String name;
        private String valueName;
    }

    /**
     * 商品详情（属性列表 + 详情图片）。
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DetailVO implements Serializable {
        private List<PropertyVO> properties;
        private List<String> pictures;
    }

    /**
     * 详情属性（名称 + 值）。
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PropertyVO implements Serializable {
        private String name;
        private String value;
    }

    /**
     * 品牌信息。
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BrandVO implements Serializable {
        private String name;
    }
}