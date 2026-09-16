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
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 商品实体，对应表 goods。
 * <p>
 * 注意：前端接口中商品 id 为字符串，因此本表 id 使用 VARCHAR；price 使用 DECIMAL。
 * 描述字段名为 description，与数据库列名一致，避免把 MySQL 保留字 desc 当作列别名。
 */
@Data
@TableName("goods")
public class Goods implements Serializable {

    /** 商品 ID（字符串） */
    @TableId(type = IdType.INPUT)
    private String id;

    /** 商品名称 */
    private String name;

    /** 商品描述（列名 description，避免 MySQL 保留字 desc 作为列别名） */
    private String description;

    /** 售价 */
    private BigDecimal price;

    /** 原价（划线价，可为空） */
    private BigDecimal oldPrice;

    /** 封面图 URL */
    private String picture;

    /** 主图集合，JSON 数组字符串 */
    private String mainPictures;

    /** 所属二级分类 ID */
    private String categoryId;

    /** 品牌名称 */
    private String brand;

    /** 销量 */
    private Integer salesCount;

    /** 评论数 */
    private Integer commentCount;

    /** 收藏数 */
    private Integer collectCount;

    /** 销量/排序号（用于 "人气推荐" 排序） */
    private Integer orderNum;

    /** 是否新品：1 是 0 否（首页"新鲜好物"） */
    private Integer isNew;

    /** 上架状态：1 上架 0 下架 */
    private Integer status;

    /** 商品详情，JSON 字符串：{"properties":[{"name","value"}],"pictures":["..."]} */
    private String details;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @TableLogic
    @JsonIgnore
    private Integer isDeleted;
}