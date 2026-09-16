package com.tuxian.pojo.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * 二级分类商品列表查询参数（对应前端 /category/goods/temporary）。
 */
@Data
public class SubCategoryQueryDTO implements Serializable {

    /** 二级分类 ID */
    private String categoryId;

    /** 页码 */
    private Integer page = 1;

    /** 每页条数 */
    private Integer pageSize = 20;

    /** 排序字段：publishTime / orderNum / evaluateNum */
    private String sortField = "publishTime";
}