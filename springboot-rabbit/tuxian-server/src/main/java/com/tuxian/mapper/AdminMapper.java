package com.tuxian.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.tuxian.pojo.entity.Admin;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface AdminMapper extends BaseMapper<Admin> {
}