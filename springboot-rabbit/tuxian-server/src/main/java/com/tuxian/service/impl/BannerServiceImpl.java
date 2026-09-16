package com.tuxian.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.tuxian.mapper.BannerMapper;
import com.tuxian.pojo.entity.Banner;
import com.tuxian.service.BannerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class BannerServiceImpl implements BannerService {

    @Autowired
    private BannerMapper bannerMapper;

    @Override
    public List<Banner> listByDistributionSite(Integer distributionSite) {
        return bannerMapper.selectList(new LambdaQueryWrapper<Banner>()
                .eq(Banner::getDistributionSite, distributionSite)
                .orderByAsc(Banner::getSort));
    }
}