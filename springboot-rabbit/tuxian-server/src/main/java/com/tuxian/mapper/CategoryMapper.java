package com.tuxian.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.tuxian.pojo.entity.Category;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface CategoryMapper extends BaseMapper<Category> {
}