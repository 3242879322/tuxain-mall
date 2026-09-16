package com.tuxian.controller;

import com.tuxian.common.result.PageResult;
import com.tuxian.common.result.Result;
import com.tuxian.pojo.dto.SubCategoryQueryDTO;
import com.tuxian.pojo.vo.CategoryVO;
import com.tuxian.pojo.vo.GoodsVO;
import com.tuxian.pojo.vo.SubCategoryFilterVO;
import com.tuxian.service.CategoryService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 商品分类相关接口。
 */
@RestController
@RequestMapping("/category")
@Api(tags = "商品分类相关接口")
public class CategoryController {

    @Autowired
    private CategoryService categoryService;

    @GetMapping
    @ApiOperation("获取分类数据（含二级分类与商品）")
    public Result<CategoryVO> category(@RequestParam String id) {
        return Result.success(categoryService.getCategoryById(id));
    }

    @GetMapping("/sub/filter")
    @ApiOperation("获取二级分类面包屑")
    public Result<SubCategoryFilterVO> subFilter(@RequestParam String id) {
        return Result.success(categoryService.getSubFilter(id));
    }

    @PostMapping("/goods/temporary")
    @ApiOperation("获取二级分类商品列表")
    public Result<PageResult<GoodsVO>> subCategoryGoods(@RequestBody SubCategoryQueryDTO queryDTO) {
        return Result.success(categoryService.getSubCategoryGoods(queryDTO));
    }
}
