package com.tuxian.service;

import com.tuxian.common.result.PageResult;
import com.tuxian.pojo.dto.SubCategoryQueryDTO;
import com.tuxian.pojo.vo.CategoryHeadVO;
import com.tuxian.pojo.vo.CategoryVO;
import com.tuxian.pojo.vo.GoodsVO;
import com.tuxian.pojo.vo.SubCategoryFilterVO;

import java.util.List;

public interface CategoryService {

    /** 首页分类导航（/home/category/head） */
    List<CategoryHeadVO> listHead();

    /** 分类页数据（/category?id） */
    CategoryVO getCategoryById(String id);

    /** 二级分类面包屑（/category/sub/filter?id） */
    SubCategoryFilterVO getSubFilter(String id);

    /** 二级分类商品列表（/category/goods/temporary） */
    PageResult<GoodsVO> getSubCategoryGoods(SubCategoryQueryDTO queryDTO);
}