package com.tuxian.pojo.dto;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 删除购物车请求参数（前端 DELETE /member/cart 的请求体：{ ids: [...] }）。
 */
@Data
public class CartDeleteDTO implements Serializable {

    private List<String> ids;
}