package com.tuxian.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.tuxian.common.constant.MessageConstant;
import com.tuxian.common.context.BaseContext;
import com.tuxian.common.exception.BaseException;
import com.tuxian.mapper.UserAddressMapper;
import com.tuxian.pojo.dto.AddressDTO;
import com.tuxian.pojo.entity.UserAddress;
import com.tuxian.service.AddressService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class AddressServiceImpl implements AddressService {

    /** 默认地址标记 */
    private static final int DEFAULT = 0;
    private static final int NOT_DEFAULT = 1;

    @Autowired
    private UserAddressMapper userAddressMapper;

    @Override
    public List<UserAddress> list() {
        Long userId = BaseContext.getCurrentId();
        // 默认地址排前面，其次按创建时间倒序
        return userAddressMapper.selectList(new LambdaQueryWrapper<UserAddress>()
                .eq(UserAddress::getUserId, userId)
                .orderByAsc(UserAddress::getIsDefault)
                .orderByDesc(UserAddress::getCreateTime));
    }

    @Override
    @Transactional
    public void add(AddressDTO addressDTO) {
        Long userId = BaseContext.getCurrentId();

        UserAddress address = new UserAddress();
        BeanUtils.copyProperties(addressDTO, address);
        address.setUserId(userId);
        if (address.getIsDefault() == null) {
            address.setIsDefault(NOT_DEFAULT);
        }

        // 新增地址设置为默认时，先取消其它默认地址
        if (DEFAULT == address.getIsDefault()) {
            clearDefault(userId);
        }
        userAddressMapper.insert(address);
    }

    @Override
    @Transactional
    public void update(Long id, AddressDTO addressDTO) {
        UserAddress address = userAddressMapper.selectById(id);
        if (address == null || !address.getUserId().equals(BaseContext.getCurrentId())) {
            throw new BaseException(MessageConstant.ADDRESS_NOT_FOUND);
        }

        BeanUtils.copyProperties(addressDTO, address);
        if (DEFAULT == address.getIsDefault()) {
            clearDefault(address.getUserId());
        }
        userAddressMapper.updateById(address);
    }

    @Override
    public void delete(Long id) {
        UserAddress address = userAddressMapper.selectById(id);
        if (address == null || !address.getUserId().equals(BaseContext.getCurrentId())) {
            throw new BaseException(MessageConstant.ADDRESS_NOT_FOUND);
        }
        userAddressMapper.deleteById(id);
    }

    @Override
    @Transactional
    public void setDefault(Long id) {
        UserAddress address = userAddressMapper.selectById(id);
        if (address == null || !address.getUserId().equals(BaseContext.getCurrentId())) {
            throw new BaseException(MessageConstant.ADDRESS_NOT_FOUND);
        }
        clearDefault(address.getUserId());
        address.setIsDefault(DEFAULT);
        userAddressMapper.updateById(address);
    }

    /**
     * 把该用户的所有地址置为非默认。
     */
    private void clearDefault(Long userId) {
        userAddressMapper.update(null, new LambdaUpdateWrapper<UserAddress>()
                .eq(UserAddress::getUserId, userId)
                .set(UserAddress::getIsDefault, NOT_DEFAULT));
    }
}