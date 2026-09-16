package com.tuxian.pojo.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import java.io.Serializable;

/**
 * 新增/修改收货地址请求参数。
 */
@Data
public class AddressDTO implements Serializable {

    /** 收货人 */
    @NotBlank(message = "收货人不能为空")
    private String receiver;

    /** 联系方式 */
    @NotBlank(message = "联系方式不能为空")
    private String contact;

    /** 省份编码 */
    private String provinceCode;

    /** 城市编码 */
    private String cityCode;

    /** 区县编码 */
    private String countyCode;

    /** 省市区完整名称 */
    private String fullLocation;

    /** 详细地址 */
    @NotBlank(message = "详细地址不能为空")
    private String address;

    /** 是否默认：0 默认 1 非默认 */
    private Integer isDefault;
}