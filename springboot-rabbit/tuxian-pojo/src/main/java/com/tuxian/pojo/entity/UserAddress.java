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
 * 收货地址实体，对应表 user_address。
 */
@Data
@TableName("user_address")
public class UserAddress implements Serializable {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 所属用户 ID */
    private Long userId;

    /** 收货人 */
    private String receiver;

    /** 联系方式 */
    private String contact;

    /** 省份编码 */
    private String provinceCode;

    /** 城市编码 */
    private String cityCode;

    /** 区县编码 */
    private String countyCode;

    /** 省市区完整名称，如 "广东省 广州市 天河区" */
    private String fullLocation;

    /** 详细地址 */
    private String address;

    /** 是否默认：0 默认 1 非默认（前端约定） */
    private Integer isDefault;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @TableLogic
    @JsonIgnore
    private Integer isDeleted;
}