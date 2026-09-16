package com.tuxian.pojo.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

/**
 * 首页"一站买全" VO（每个一级分类一个"馆"）。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HomeGoodsVO implements Serializable {

    private String id;
    private String name;
    private String picture;
    /** 卖点文案，如 "¥199" */
    private String saleInfo;
    /** 该馆下的商品列表 */
    private List<GoodsVO> goods;
}