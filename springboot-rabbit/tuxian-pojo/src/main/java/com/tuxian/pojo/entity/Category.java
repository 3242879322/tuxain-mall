package com.tuxian.pojo.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 商品分类实体，对应表 category。
 * <p>
 * 分类为两级：level = 1 一级分类，level = 2 二级分类；parentId 指向一级分类。
 */
@Data
@TableName("category")
public class Category implements Serializable {

    /** 分类 ID（字符串，前端路由/接口均以字符串传递） */
    @TableId(type = IdType.INPUT)
    private String id;

    /** 分类名称 */
    private String name;

    /** 父分类 ID（一级分类为空） */
    private String parentId;

    /** 分类层级：1 一级 2 二级 */
    private Integer level;

    /** 分类图片（二级分类使用） */
    private String picture;

    /** 分类卖点文案（如 "¥199"） */
    private String saleInfo;

    /** 排序权重 */
    private Integer sort;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @TableLogic
    @JsonIgnore
    private Integer isDeleted;
}