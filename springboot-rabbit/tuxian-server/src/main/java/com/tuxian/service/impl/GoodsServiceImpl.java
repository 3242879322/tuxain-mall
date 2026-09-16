package com.tuxian.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tuxian.common.constant.MessageConstant;
import com.tuxian.common.exception.BaseException;
import com.tuxian.mapper.CategoryMapper;
import com.tuxian.mapper.GoodsMapper;
import com.tuxian.mapper.GoodsSkuMapper;
import com.tuxian.pojo.entity.Category;
import com.tuxian.pojo.entity.Goods;
import com.tuxian.pojo.entity.GoodsSku;
import com.tuxian.pojo.vo.GoodsDetailVO;
import com.tuxian.pojo.vo.GoodsVO;
import com.tuxian.pojo.vo.HomeGoodsVO;
import com.tuxian.pojo.vo.HotGoodsVO;
import com.tuxian.service.GoodsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 商品服务实现。
 */
@Service
public class GoodsServiceImpl implements GoodsService {

    @Autowired
    private GoodsMapper goodsMapper;

    @Autowired
    private GoodsSkuMapper goodsSkuMapper;

    @Autowired
    private CategoryMapper categoryMapper;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    @Cacheable(cacheNames = "goodsDetail", key = "#id")
    public GoodsDetailVO getDetail(String id) {
        Goods goods = goodsMapper.selectById(id);
        if (goods == null) {
            throw new BaseException(MessageConstant.GOODS_NOT_FOUND);
        }

        List<GoodsSku> skus = goodsSkuMapper.selectList(new LambdaQueryWrapper<GoodsSku>()
                .eq(GoodsSku::getGoodsId, id));

        // 面包屑分类：[0] 二级分类 [1] 一级分类
        List<GoodsDetailVO.CategoryVO> categories = buildCategories(goods.getCategoryId());

        return GoodsDetailVO.builder()
                .id(goods.getId())
                .name(goods.getName())
                .desc(goods.getDescription())
                .price(goods.getPrice())
                .oldPrice(goods.getOldPrice())
                .mainPictures(parseJsonArray(goods.getMainPictures()))
                .categories(categories)
                .specs(buildSpecs(skus))
                .skus(buildSkuVOs(skus))
                .details(buildDetails(goods.getDetails()))
                .brand(GoodsDetailVO.BrandVO.builder().name(goods.getBrand()).build())
                .salesCount(goods.getSalesCount())
                .commentCount(goods.getCommentCount())
                .collectCount(goods.getCollectCount())
                .build();
    }

    @Override
    public List<GoodsVO> getHotGoods(String id, Integer type, Integer limit) {
        if (limit == null || limit <= 0) {
            limit = 3;
        }
        LambdaQueryWrapper<Goods> wrapper = new LambdaQueryWrapper<Goods>()
                .eq(Goods::getStatus, 1);

        // 尽量返回同分类商品
        Goods goods = goodsMapper.selectById(id);
        if (goods != null) {
            wrapper.eq(Goods::getCategoryId, goods.getCategoryId()).ne(Goods::getId, id);
        }
        wrapper.orderByDesc(Goods::getOrderNum).last("limit " + limit);

        return goodsMapper.selectList(wrapper).stream().map(this::toGoodsVO).collect(Collectors.toList());
    }

    @Override
    public List<GoodsVO> getRelevant(Integer limit) {
        if (limit == null || limit <= 0) {
            limit = 4;
        }
        List<Goods> goods = goodsMapper.selectList(new LambdaQueryWrapper<Goods>()
                .eq(Goods::getStatus, 1)
                .orderByDesc(Goods::getOrderNum)
                .last("limit " + limit));
        return goods.stream().map(this::toGoodsVO).collect(Collectors.toList());
    }

    @Override
    @Cacheable(cacheNames = "homeNewGoods", key = "'new'")
    public List<GoodsVO> getNewGoods() {
        List<Goods> goods = goodsMapper.selectList(new LambdaQueryWrapper<Goods>()
                .eq(Goods::getStatus, 1)
                .eq(Goods::getIsNew, 1)
                .orderByDesc(Goods::getCreateTime)
                .last("limit 10"));
        return goods.stream().map(this::toGoodsVO).collect(Collectors.toList());
    }

    @Override
    @Cacheable(cacheNames = "homeHotGoods", key = "'hot'")
    public List<HotGoodsVO> getHotList() {
        List<Goods> goods = goodsMapper.selectList(new LambdaQueryWrapper<Goods>()
                .eq(Goods::getStatus, 1)
                .orderByDesc(Goods::getOrderNum)
                .last("limit 10"));
        return goods.stream()
                .map(g -> HotGoodsVO.builder()
                        .id(g.getId())
                        .title(g.getName())
                        .alt(g.getDescription())
                        .picture(g.getPicture())
                        .build())
                .collect(Collectors.toList());
    }

    @Override
    @Cacheable(cacheNames = "homeGoods", key = "'homeGoods'")
    public List<HomeGoodsVO> getHomeGoods() {
        // 一级分类，每个分类作为一个"馆"
        List<Category> level1List = categoryMapper.selectList(new LambdaQueryWrapper<Category>()
                .eq(Category::getLevel, 1)
                .orderByAsc(Category::getSort));

        return level1List.stream().map(level1 -> {
            List<String> childIds = categoryMapper.selectList(new LambdaQueryWrapper<Category>()
                            .eq(Category::getParentId, level1.getId()))
                    .stream().map(Category::getId).collect(Collectors.toList());

            List<GoodsVO> goods = listGoodsByCategoryIds(childIds, 8);
            return HomeGoodsVO.builder()
                    .id(level1.getId())
                    .name(level1.getName())
                    .picture(level1.getPicture())
                    .saleInfo(level1.getSaleInfo())
                    .goods(goods)
                    .build();
        }).collect(Collectors.toList());
    }

    // ==================== 私有辅助方法 ====================

    private GoodsVO toGoodsVO(Goods goods) {
        return GoodsVO.builder()
                .id(goods.getId())
                .name(goods.getName())
                .desc(goods.getDescription())
                .price(GoodsVO.formatPrice(goods.getPrice()))
                .picture(goods.getPicture())
                .build();
    }

    private List<GoodsVO> listGoodsByCategoryIds(List<String> categoryIds, int limit) {
        if (categoryIds == null || categoryIds.isEmpty()) {
            return List.of();
        }
        return goodsMapper.selectList(new LambdaQueryWrapper<Goods>()
                        .in(Goods::getCategoryId, categoryIds)
                        .eq(Goods::getStatus, 1)
                        .orderByDesc(Goods::getOrderNum)
                        .last("limit " + limit))
                .stream().map(this::toGoodsVO).collect(Collectors.toList());
    }

    /**
     * 构建面包屑分类：[0] 二级分类 [1] 一级分类。
     */
    private List<GoodsDetailVO.CategoryVO> buildCategories(String subCategoryId) {
        List<GoodsDetailVO.CategoryVO> result = new ArrayList<>();
        Category sub = categoryMapper.selectById(subCategoryId);
        if (sub != null) {
            result.add(GoodsDetailVO.CategoryVO.builder().id(sub.getId()).name(sub.getName()).build());
            Category parent = categoryMapper.selectById(sub.getParentId());
            if (parent != null) {
                result.add(GoodsDetailVO.CategoryVO.builder().id(parent.getId()).name(parent.getName()).build());
            }
        }
        return result;
    }

    /**
     * 从 SKU 组合中聚合出规格定义（规格名 -> 去重后的可选值）。
     */
    private List<GoodsDetailVO.SpecVO> buildSpecs(List<GoodsSku> skus) {
        Map<String, LinkedHashSet<String>> specMap = new LinkedHashMap<>();
        for (GoodsSku sku : skus) {
            List<GoodsDetailVO.SkuSpecVO> specs = parseSpecs(sku.getSpecs());
            for (GoodsDetailVO.SkuSpecVO spec : specs) {
                specMap.computeIfAbsent(spec.getName(), k -> new LinkedHashSet<>()).add(spec.getValueName());
            }
        }

        List<GoodsDetailVO.SpecVO> result = new ArrayList<>();
        for (Map.Entry<String, LinkedHashSet<String>> entry : specMap.entrySet()) {
            List<GoodsDetailVO.SpecValueVO> values = entry.getValue().stream()
                    .map(v -> GoodsDetailVO.SpecValueVO.builder().name(v).build())
                    .collect(Collectors.toList());
            result.add(GoodsDetailVO.SpecVO.builder()
                    .id(entry.getKey())
                    .name(entry.getKey())
                    .values(values)
                    .build());
        }
        return result;
    }

    private List<GoodsDetailVO.SkuVO> buildSkuVOs(List<GoodsSku> skus) {
        return skus.stream().map(sku -> GoodsDetailVO.SkuVO.builder()
                .id(sku.getId())
                .price(sku.getPrice())
                .oldPrice(sku.getOldPrice())
                .inventory(sku.getInventory())
                .specs(parseSpecs(sku.getSpecs()))
                .build()).collect(Collectors.toList());
    }

    /**
     * 解析商品详情 JSON（properties + pictures）。
     */
    private GoodsDetailVO.DetailVO buildDetails(String detailsJson) {
        try {
            if (detailsJson == null || detailsJson.isEmpty()) {
                return emptyDetail();
            }
            return objectMapper.readValue(detailsJson, new TypeReference<GoodsDetailVO.DetailVO>() {
            });
        } catch (Exception e) {
            return emptyDetail();
        }
    }

    private GoodsDetailVO.DetailVO emptyDetail() {
        return GoodsDetailVO.DetailVO.builder().properties(new ArrayList<>()).pictures(new ArrayList<>()).build();
    }

    private List<String> parseJsonArray(String json) {
        try {
            if (json == null || json.isEmpty()) {
                return new ArrayList<>();
            }
            return objectMapper.readValue(json, new TypeReference<List<String>>() {
            });
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }

    private List<GoodsDetailVO.SkuSpecVO> parseSpecs(String json) {
        try {
            if (json == null || json.isEmpty()) {
                return new ArrayList<>();
            }
            return objectMapper.readValue(json, new TypeReference<List<GoodsDetailVO.SkuSpecVO>>() {
            });
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }
}