package com.tuxian.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tuxian.common.constant.MessageConstant;
import com.tuxian.common.exception.BaseException;
import com.tuxian.common.result.PageResult;
import com.tuxian.mapper.CategoryMapper;
import com.tuxian.mapper.GoodsMapper;
import com.tuxian.pojo.dto.SubCategoryQueryDTO;
import com.tuxian.pojo.entity.Category;
import com.tuxian.pojo.entity.Goods;
import com.tuxian.pojo.vo.CategoryHeadVO;
import com.tuxian.pojo.vo.CategoryVO;
import com.tuxian.pojo.vo.GoodsVO;
import com.tuxian.pojo.vo.SubCategoryFilterVO;
import com.tuxian.service.CategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class CategoryServiceImpl implements CategoryService {

    @Autowired
    private CategoryMapper categoryMapper;

    @Autowired
    private GoodsMapper goodsMapper;

    @Override
    @Cacheable(cacheNames = "categoryHead", key = "'head'")
    public List<CategoryHeadVO> listHead() {
        // 一级分类
        List<Category> level1List = listLevel1();
        return level1List.stream().map(level1 -> {
            List<Category> children = listChildren(level1.getId());
            List<CategoryHeadVO.ChildVO> childVOs = children.stream()
                    .map(c -> CategoryHeadVO.ChildVO.builder().id(c.getId()).name(c.getName()).build())
                    .collect(Collectors.toList());

            // 每个一级分类下推荐几个商品（取所有二级分类下的商品）
            List<String> childIds = children.stream().map(Category::getId).collect(Collectors.toList());
            List<GoodsVO> goods = listGoodsByCategoryIds(childIds, 4);

            return CategoryHeadVO.builder()
                    .id(level1.getId())
                    .name(level1.getName())
                    .children(childVOs)
                    .goods(goods)
                    .build();
        }).collect(Collectors.toList());
    }

    @Override
    @Cacheable(cacheNames = "category", key = "#id")
    public CategoryVO getCategoryById(String id) {
        Category level1 = categoryMapper.selectById(id);
        if (level1 == null) {
            throw new BaseException(MessageConstant.CATEGORY_NOT_FOUND);
        }

        List<Category> children = listChildren(id);
        List<CategoryVO.ChildVO> childVOs = children.stream().map(child -> {
            List<GoodsVO> goods = listGoodsByCategoryIds(List.of(child.getId()), 6);
            return CategoryVO.ChildVO.builder()
                    .id(child.getId())
                    .name(child.getName())
                    .picture(child.getPicture())
                    .goods(goods)
                    .build();
        }).collect(Collectors.toList());

        return CategoryVO.builder()
                .id(level1.getId())
                .name(level1.getName())
                .children(childVOs)
                .build();
    }

    @Override
    @Cacheable(cacheNames = "subCategoryFilter", key = "#id")
    public SubCategoryFilterVO getSubFilter(String id) {
        Category sub = categoryMapper.selectById(id);
        if (sub == null) {
            throw new BaseException(MessageConstant.CATEGORY_NOT_FOUND);
        }
        Category parent = categoryMapper.selectById(sub.getParentId());
        return SubCategoryFilterVO.builder()
                .id(sub.getId())
                .name(sub.getName())
                .parentId(sub.getParentId())
                .parentName(parent != null ? parent.getName() : null)
                .build();
    }

    @Override
    public PageResult<GoodsVO> getSubCategoryGoods(SubCategoryQueryDTO queryDTO) {
        LambdaQueryWrapper<Goods> wrapper = new LambdaQueryWrapper<Goods>()
                .eq(Goods::getCategoryId, queryDTO.getCategoryId())
                .eq(Goods::getStatus, 1);

        // 排序：publishTime 按时间，orderNum 按销量，evaluateNum 按评论数
        String sortField = queryDTO.getSortField();
        if ("orderNum".equals(sortField)) {
            wrapper.orderByDesc(Goods::getOrderNum);
        } else if ("evaluateNum".equals(sortField)) {
            wrapper.orderByDesc(Goods::getCommentCount);
        } else {
            wrapper.orderByDesc(Goods::getCreateTime);
        }

        Page<Goods> page = new Page<>(queryDTO.getPage(), queryDTO.getPageSize());
        Page<Goods> result = goodsMapper.selectPage(page, wrapper);

        List<GoodsVO> items = result.getRecords().stream().map(this::toGoodsVO).collect(Collectors.toList());
        return new PageResult<>(items, result.getTotal(), queryDTO.getPage(), queryDTO.getPageSize());
    }

    // ==================== 私有辅助方法 ====================

    private List<Category> listLevel1() {
        return categoryMapper.selectList(new LambdaQueryWrapper<Category>()
                .eq(Category::getLevel, 1)
                .orderByAsc(Category::getSort));
    }

    private List<Category> listChildren(String parentId) {
        return categoryMapper.selectList(new LambdaQueryWrapper<Category>()
                .eq(Category::getParentId, parentId)
                .orderByAsc(Category::getSort));
    }

    private List<GoodsVO> listGoodsByCategoryIds(List<String> categoryIds, int limit) {
        if (categoryIds == null || categoryIds.isEmpty()) {
            return List.of();
        }
        List<Goods> goods = goodsMapper.selectList(new LambdaQueryWrapper<Goods>()
                .in(Goods::getCategoryId, categoryIds)
                .eq(Goods::getStatus, 1)
                .orderByDesc(Goods::getOrderNum)
                .last("limit " + limit));
        return goods.stream().map(this::toGoodsVO).collect(Collectors.toList());
    }

    private GoodsVO toGoodsVO(Goods goods) {
        return GoodsVO.builder()
                .id(goods.getId())
                .name(goods.getName())
                .desc(goods.getDescription())
                .price(GoodsVO.formatPrice(goods.getPrice()))
                .picture(goods.getPicture())
                .build();
    }
}