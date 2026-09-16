package com.tuxian.pojo.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 二级分类面包屑 VO（/category/sub/filter?id=xxx）。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SubCategoryFilterVO implements Serializable {

    private String id;
    private String name;

    /** 父分类 ID */
    private String parentId;
    /** 父分类名称 */
    private String parentName;
}