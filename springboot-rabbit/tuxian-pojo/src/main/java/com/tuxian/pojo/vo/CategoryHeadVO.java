package com.tuxian.pojo.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

/**
 * 首页分类导航 VO（/home/category/head）。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CategoryHeadVO implements Serializable {

    private String id;
    private String name;

    /** 二级分类（导航顶部只展示前两个） */
    private List<ChildVO> children;

    /** 该分类下的推荐商品（悬浮层展示） */
    private List<GoodsVO> goods;

    /**
     * 二级分类简要信息。
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ChildVO implements Serializable {
        private String id;
        private String name;
    }
}