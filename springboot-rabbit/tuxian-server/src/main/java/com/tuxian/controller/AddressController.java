package com.tuxian.controller;

import com.tuxian.common.result.Result;
import com.tuxian.pojo.dto.AddressDTO;
import com.tuxian.pojo.entity.UserAddress;
import com.tuxian.service.AddressService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 收货地址相关接口。
 */
@RestController
@RequestMapping("/member/address")
@Api(tags = "收货地址相关接口")
public class AddressController {

    @Autowired
    private AddressService addressService;

    @GetMapping
    @ApiOperation("获取收货地址列表")
    public Result<List<UserAddress>> list() {
        return Result.success(addressService.list());
    }

    @PostMapping
    @ApiOperation("新增收货地址")
    public Result<Void> add(@RequestBody @Validated AddressDTO addressDTO) {
        addressService.add(addressDTO);
        return Result.success();
    }

    @PutMapping("/{id}")
    @ApiOperation("修改收货地址")
    public Result<Void> update(@PathVariable Long id, @RequestBody @Validated AddressDTO addressDTO) {
        addressService.update(id, addressDTO);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    @ApiOperation("删除收货地址")
    public Result<Void> delete(@PathVariable Long id) {
        addressService.delete(id);
        return Result.success();
    }

    @PutMapping("/{id}/default")
    @ApiOperation("设置默认地址")
    public Result<Void> setDefault(@PathVariable Long id) {
        addressService.setDefault(id);
        return Result.success();
    }
}
