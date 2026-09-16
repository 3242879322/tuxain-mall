package com.tuxian.pojo.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

/**
 * 分类页数据 VO（/category?id=xxx，返回一级分类及其下所有二级分类与商品）。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CategoryVO implements Serializable {

    private String id;
    private String name;

    /** 二级分类列表 */
    private List<ChildVO> children;

    /**
     * 二级分类（含图片与商品）。
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ChildVO implements Serializable {
        private String id;
        private String name;
        private String picture;
        private List<GoodsVO> goods;
    }
}