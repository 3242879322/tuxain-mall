package com.tuxian.controller;

import com.tuxian.common.result.Result;
import com.tuxian.pojo.vo.GoodsDetailVO;
import com.tuxian.pojo.vo.GoodsVO;
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
 * 商品相关接口（详情、热榜、猜你喜欢）。
 */
@RestController
@RequestMapping("/goods")
@Api(tags = "商品相关接口")
public class GoodsController {

    @Autowired
    private GoodsService goodsService;

    @GetMapping
    @ApiOperation("获取商品详情")
    public Result<GoodsDetailVO> detail(@RequestParam String id) {
        return Result.success(goodsService.getDetail(id));
    }

    @GetMapping("/hot")
    @ApiOperation("获取热榜商品")
    public Result<List<GoodsVO>> hot(@RequestParam String id,
                                     @RequestParam(defaultValue = "1") Integer type,
                                     @RequestParam(defaultValue = "3") Integer limit) {
        return Result.success(goodsService.getHotGoods(id, type, limit));
    }

    @GetMapping("/relevant")
    @ApiOperation("获取猜你喜欢")
    public Result<List<GoodsVO>> relevant(@RequestParam(defaultValue = "4") Integer limit) {
        return Result.success(goodsService.getRelevant(limit));
    }
}
