package com.tuxian.controller;

import com.tuxian.common.result.Result;
import com.tuxian.pojo.entity.Banner;
import com.tuxian.pojo.vo.CategoryHeadVO;
import com.tuxian.pojo.vo.GoodsVO;
import com.tuxian.pojo.vo.HomeGoodsVO;
import com.tuxian.pojo.vo.HotGoodsVO;
import com.tuxian.service.BannerService;
import com.tuxian.service.CategoryService;
import com.tuxian.service.GoodsService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 首页相关接口（轮播图、新鲜好物、人气推荐、一站买全、分类导航）。
 */
@RestController
@RequestMapping("/home")
@Api(tags = "首页相关接口")
public class HomeController {

    @Autowired
    private BannerService bannerService;

    @Autowired
    private GoodsService goodsService;

    @Autowired
    private CategoryService categoryService;

    @GetMapping("/banner")
    @ApiOperation("获取轮播图")
    public Result<List<Banner>> banner(@RequestParam(defaultValue = "1") Integer distributionSite) {
        return Result.success(bannerService.listByDistributionSite(distributionSite));
    }

    @GetMapping("/new")
    @ApiOperation("获取新鲜好物")
    public Result<List<GoodsVO>> newGoods() {
        return Result.success(goodsService.getNewGoods());
    }

    @GetMapping("/hot")
    @ApiOperation("获取人气推荐")
    public Result<List<HotGoodsVO>> hot() {
        return Result.success(goodsService.getHotList());
    }

    @GetMapping("/goods")
    @ApiOperation("获取一站买全")
    public Result<List<HomeGoodsVO>> goods() {
        return Result.success(goodsService.getHomeGoods());
    }

    @GetMapping("/category/head")
    @ApiOperation("获取首页分类导航")
    public Result<List<CategoryHeadVO>> categoryHead() {
        return Result.success(categoryService.listHead());
    }
}
