package com.tuxian.service;

import com.tuxian.pojo.vo.GoodsDetailVO;
import com.tuxian.pojo.vo.GoodsVO;
import com.tuxian.pojo.vo.HomeGoodsVO;
import com.tuxian.pojo.vo.HotGoodsVO;

import java.util.List;

public interface GoodsService {

    /** 商品详情（/goods?id） */
    GoodsDetailVO getDetail(String id);

    /** 详情页热榜（/goods/hot） */
    List<GoodsVO> getHotGoods(String id, Integer type, Integer limit);

    /** 猜你喜欢（/goods/relevant） */
    List<GoodsVO> getRelevant(Integer limit);

    /** 首页新鲜好物（/home/new） */
    List<GoodsVO> getNewGoods();

    /** 首页人气推荐（/home/hot） */
    List<HotGoodsVO> getHotList();

    /** 首页一站买全（/home/goods） */
    List<HomeGoodsVO> getHomeGoods();
}