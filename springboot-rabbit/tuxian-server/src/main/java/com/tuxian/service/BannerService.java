package com.tuxian.service;

import com.tuxian.pojo.entity.Banner;

import java.util.List;

public interface BannerService {

    /**
     * 按投放位置查询轮播图（1 首页 2 分类页）。
     */
    List<Banner> listByDistributionSite(Integer distributionSite);
}