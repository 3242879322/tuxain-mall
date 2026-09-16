package com.tuxian.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tuxian.common.constant.MessageConstant;
import com.tuxian.common.constant.RedisConstant;
import com.tuxian.common.context.BaseContext;
import com.tuxian.common.exception.BaseException;
import com.tuxian.mapper.GoodsMapper;
import com.tuxian.mapper.GoodsSkuMapper;
import com.tuxian.pojo.dto.CartItemDTO;
import com.tuxian.pojo.dto.CartMergeItemDTO;
import com.tuxian.pojo.entity.Goods;
import com.tuxian.pojo.entity.GoodsSku;
import com.tuxian.pojo.vo.CartVO;
import com.tuxian.pojo.vo.GoodsDetailVO;
import com.tuxian.service.CartService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 购物车服务实现。
 * <p>
 * 购物车数据存放在 Redis Hash 中：
 * key = cart:{userId}，field = skuId，value = 购物车项 JSON。
 * 读写不经过 MySQL，性能高；退出登录/更换设备通过 merge 接口合并。
 */
@Service
public class CartServiceImpl implements CartService {

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Autowired
    private GoodsSkuMapper goodsSkuMapper;

    @Autowired
    private GoodsMapper goodsMapper;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public List<CartVO> list() {
        Map<Object, Object> entries = stringRedisTemplate.opsForHash().entries(cartKey());
        return entries.values().stream()
                .map(v -> parseCart((String) v))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    @Override
    public void add(CartItemDTO cartItemDTO) {
        String key = cartKey();
        GoodsSku sku = goodsSkuMapper.selectById(cartItemDTO.getSkuId());
        if (sku == null) {
            throw new BaseException(MessageConstant.SKU_NOT_FOUND);
        }

        CartVO existing = getCartItem(key, cartItemDTO.getSkuId());
        int total = (existing == null ? 0 : existing.getCount()) + cartItemDTO.getCount();
        if (total > sku.getInventory()) {
            throw new BaseException(MessageConstant.SKU_STOCK_NOT_ENOUGH);
        }

        if (existing != null) {
            existing.setCount(total);
            put(key, cartItemDTO.getSkuId(), existing);
        } else {
            put(key, cartItemDTO.getSkuId(), buildCartItem(sku, cartItemDTO.getCount(), true));
        }
    }

    @Override
    public void update(CartMergeItemDTO dto) {
        String key = cartKey();
        CartVO existing = getCartItem(key, dto.getSkuId());
        if (existing == null) {
            throw new BaseException(MessageConstant.CART_ITEM_NOT_FOUND);
        }
        if (dto.getCount() != null) {
            existing.setCount(dto.getCount());
        }
        if (dto.getSelected() != null) {
            existing.setSelected(dto.getSelected());
        }
        put(key, dto.getSkuId(), existing);
    }

    @Override
    public void updateSelected(List<CartMergeItemDTO> dtos) {
        String key = cartKey();
        for (CartMergeItemDTO dto : dtos) {
            CartVO existing = getCartItem(key, dto.getSkuId());
            if (existing != null && dto.getSelected() != null) {
                existing.setSelected(dto.getSelected());
                put(key, dto.getSkuId(), existing);
            }
        }
    }

    @Override
    public void delete(List<String> ids) {
        if (ids == null || ids.isEmpty()) {
            return;
        }
        String key = cartKey();
        for (String id : ids) {
            stringRedisTemplate.opsForHash().delete(key, id);
        }
    }

    @Override
    public void merge(List<CartMergeItemDTO> dtos) {
        if (dtos == null || dtos.isEmpty()) {
            return;
        }
        String key = cartKey();
        for (CartMergeItemDTO dto : dtos) {
            CartVO existing = getCartItem(key, dto.getSkuId());
            int count = dto.getCount() == null ? 1 : dto.getCount();
            if (existing != null) {
                existing.setCount(existing.getCount() + count);
                if (dto.getSelected() != null) {
                    existing.setSelected(dto.getSelected());
                }
                put(key, dto.getSkuId(), existing);
            } else {
                GoodsSku sku = goodsSkuMapper.selectById(dto.getSkuId());
                if (sku == null) {
                    continue;
                }
                put(key, dto.getSkuId(), buildCartItem(sku, count, dto.getSelected() != null ? dto.getSelected() : true));
            }
        }
    }

    // ==================== 私有辅助方法 ====================

    private String cartKey() {
        return RedisConstant.CART_KEY_PREFIX + BaseContext.getCurrentId();
    }

    private CartVO getCartItem(String key, String skuId) {
        Object value = stringRedisTemplate.opsForHash().get(key, skuId);
        return value == null ? null : parseCart((String) value);
    }

    private void put(String key, String skuId, CartVO cartVO) {
        stringRedisTemplate.opsForHash().put(key, skuId, toJson(cartVO));
    }

    /**
     * 根据 SKU 构建购物车项（快照商品名称、图片、价格、规格文本）。
     */
    private CartVO buildCartItem(GoodsSku sku, int count, boolean selected) {
        Goods goods = goodsMapper.selectById(sku.getGoodsId());
        if (goods == null) {
            throw new BaseException(MessageConstant.GOODS_NOT_FOUND);
        }
        return CartVO.builder()
                .id(sku.getId())
                .skuId(sku.getId())
                .name(goods.getName())
                .picture(goods.getPicture())
                .price(sku.getPrice())
                .count(count)
                .attrsText(buildAttrsText(sku.getSpecs()))
                .selected(selected)
                .build();
    }

    private String buildAttrsText(String specsJson) {
        try {
            List<GoodsDetailVO.SkuSpecVO> specs = objectMapper.readValue(specsJson,
                    new TypeReference<List<GoodsDetailVO.SkuSpecVO>>() {
                    });
            return specs.stream()
                    .map(s -> s.getName() + ":" + s.getValueName())
                    .collect(Collectors.joining(" "));
        } catch (Exception e) {
            return "";
        }
    }

    private String toJson(CartVO cartVO) {
        try {
            return objectMapper.writeValueAsString(cartVO);
        } catch (Exception e) {
            throw new BaseException("购物车序列化失败");
        }
    }

    private CartVO parseCart(String json) {
        try {
            return objectMapper.readValue(json, CartVO.class);
        } catch (Exception e) {
            return null;
        }
    }
}