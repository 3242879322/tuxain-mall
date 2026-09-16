package com.tuxian.controller;

import com.tuxian.common.result.Result;
import com.tuxian.pojo.dto.CartDeleteDTO;
import com.tuxian.pojo.dto.CartItemDTO;
import com.tuxian.pojo.dto.CartMergeItemDTO;
import com.tuxian.pojo.vo.CartVO;
import com.tuxian.service.CartService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 购物车相关接口（数据存 Redis Hash）。
 */
@RestController
@RequestMapping("/member/cart")
@Api(tags = "购物车相关接口")
public class CartController {

    @Autowired
    private CartService cartService;

    @GetMapping
    @ApiOperation("获取购物车列表")
    public Result<List<CartVO>> list() {
        return Result.success(cartService.list());
    }

    @PostMapping
    @ApiOperation("加入购物车")
    public Result<Void> add(@RequestBody @Validated CartItemDTO cartItemDTO) {
        cartService.add(cartItemDTO);
        return Result.success();
    }

    @PutMapping
    @ApiOperation("修改购物车项（数量/选中）")
    public Result<Void> update(@RequestBody CartMergeItemDTO dto) {
        cartService.update(dto);
        return Result.success();
    }

    @PutMapping("/selected")
    @ApiOperation("批量修改选中状态")
    public Result<Void> updateSelected(@RequestBody List<CartMergeItemDTO> dtos) {
        cartService.updateSelected(dtos);
        return Result.success();
    }

    @DeleteMapping
    @ApiOperation("删除购物车项")
    public Result<Void> delete(@RequestBody CartDeleteDTO cartDeleteDTO) {
        cartService.delete(cartDeleteDTO.getIds());
        return Result.success();
    }

    @PostMapping("/merge")
    @ApiOperation("合并购物车（登录后合并本地购物车）")
    public Result<Void> merge(@RequestBody List<CartMergeItemDTO> dtos) {
        cartService.merge(dtos);
        return Result.success();
    }
}
