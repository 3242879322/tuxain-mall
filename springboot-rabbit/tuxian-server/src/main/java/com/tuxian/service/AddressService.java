package com.tuxian.service;

import com.tuxian.pojo.dto.AddressDTO;
import com.tuxian.pojo.entity.UserAddress;

import java.util.List;

public interface AddressService {

    /** 查询当前用户收货地址列表 */
    List<UserAddress> list();

    /** 新增收货地址 */
    void add(AddressDTO addressDTO);

    /** 修改收货地址 */
    void update(Long id, AddressDTO addressDTO);

    /** 删除收货地址 */
    void delete(Long id);

    /** 设置默认地址 */
    void setDefault(Long id);
}