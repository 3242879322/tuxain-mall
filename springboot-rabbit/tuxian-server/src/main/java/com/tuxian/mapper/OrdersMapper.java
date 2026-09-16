package com.tuxian.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.tuxian.pojo.entity.Orders;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface OrdersMapper extends BaseMapper<Orders> {
}