package com.tuxian.service;

import com.tuxian.pojo.dto.CartItemDTO;
import com.tuxian.pojo.dto.CartMergeItemDTO;
import com.tuxian.pojo.vo.CartVO;

import java.util.List;

public interface CartService {

    /** 查询购物车列表 */
    List<CartVO> list();

    /** 加入购物车 */
    void add(CartItemDTO cartItemDTO);

    /** 修改购物车项（数量/选中状态） */
    void update(CartMergeItemDTO dto);

    /** 批量修改选中状态 */
    void updateSelected(List<CartMergeItemDTO> dtos);

    /** 删除购物车项（传 skuId 数组） */
    void delete(List<String> ids);

    /** 合并购物车（登录后把本地购物车合并到服务端） */
    void merge(List<CartMergeItemDTO> dtos);
}