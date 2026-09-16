package com.tuxian.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.tuxian.pojo.entity.OrderItem;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface OrderItemMapper extends BaseMapper<OrderItem> {
}