package com.tuxian.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.tuxian.pojo.entity.Goods;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface GoodsMapper extends BaseMapper<Goods> {
}