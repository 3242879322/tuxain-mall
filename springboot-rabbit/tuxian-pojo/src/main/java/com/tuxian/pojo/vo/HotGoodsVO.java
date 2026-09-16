package com.tuxian.pojo.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 首页"人气推荐" VO。
 * <p>
 * 前端 HomeHot.vue 使用字段为 title（标题）和 alt（描述），与商品列表的 name/desc 命名不同。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HotGoodsVO implements Serializable {

    private String id;
    private String title;
    private String alt;
    private String picture;
}