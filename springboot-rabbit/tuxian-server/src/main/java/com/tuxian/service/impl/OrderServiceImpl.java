package com.tuxian.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tuxian.common.constant.MessageConstant;
import com.tuxian.common.constant.OrderStatusConstant;
import com.tuxian.common.context.BaseContext;
import com.tuxian.common.exception.BaseException;
import com.tuxian.common.result.PageResult;
import com.tuxian.mapper.GoodsMapper;
import com.tuxian.mapper.GoodsSkuMapper;
import com.tuxian.mapper.OrderItemMapper;
import com.tuxian.mapper.OrdersMapper;
import com.tuxian.mapper.UserAddressMapper;
import com.tuxian.pojo.dto.OrderSubmitDTO;
import com.tuxian.pojo.entity.Goods;
import com.tuxian.pojo.entity.GoodsSku;
import com.tuxian.pojo.entity.OrderItem;
import com.tuxian.pojo.entity.Orders;
import com.tuxian.pojo.entity.UserAddress;
import com.tuxian.pojo.vo.CartVO;
import com.tuxian.pojo.vo.GoodsDetailVO;
import com.tuxian.pojo.vo.OrderDetailVO;
import com.tuxian.pojo.vo.OrderItemVO;
import com.tuxian.pojo.vo.OrderPreVO;
import com.tuxian.pojo.vo.OrderVO;
import com.tuxian.service.CartService;
import com.tuxian.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.stream.Collectors;

/**
 * 订单服务实现。
 * <p>
 * 关键点：
 * 1. 下单、扣库存、生成订单项放在同一个事务中；
 * 2. 扣库存使用条件更新（inventory >= count 才扣），从数据库层面防止超卖；
 * 3. 订单号与商品/地址信息都做快照，保证历史订单不受后续商品改价影响。
 */
@Service
public class OrderServiceImpl implements OrderService {

    /** 待付款订单超时时间（分钟） */
    private static final int PAY_TIMEOUT_MINUTES = 30;

    @Autowired
    private OrdersMapper ordersMapper;

    @Autowired
    private OrderItemMapper orderItemMapper;

    @Autowired
    private GoodsSkuMapper goodsSkuMapper;

    @Autowired
    private GoodsMapper goodsMapper;

    @Autowired
    private UserAddressMapper userAddressMapper;

    @Autowired
    private CartService cartService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public OrderPreVO pre() {
        List<CartVO> cartList = cartService.list();
        return buildOrderPre(cartList);
    }

    @Override
    public OrderPreVO preNow(String skuId, Integer count) {
        GoodsSku sku = goodsSkuMapper.selectById(skuId);
        if (sku == null) {
            throw new BaseException(MessageConstant.SKU_NOT_FOUND);
        }
        Goods goods = goodsMapper.selectById(sku.getGoodsId());
        CartVO cartVO = CartVO.builder()
                .id(skuId)
                .skuId(skuId)
                .name(goods != null ? goods.getName() : null)
                .picture(goods != null ? goods.getPicture() : null)
                .price(sku.getPrice())
                .count(count)
                .attrsText(buildAttrsText(sku.getSpecs()))
                .selected(true)
                .build();
        return buildOrderPre(List.of(cartVO));
    }

    @Override
    @Transactional
    public String submit(OrderSubmitDTO orderSubmitDTO) {
        Long userId = BaseContext.getCurrentId();

        // 1. 校验收货地址
        UserAddress address = userAddressMapper.selectById(orderSubmitDTO.getAddressId());
        if (address == null || !address.getUserId().equals(userId)) {
            throw new BaseException(MessageConstant.ADDRESS_NOT_FOUND);
        }

        // 2. 生成订单号
        String orderId = generateOrderId();

        // 3. 扣库存 + 构建订单项 + 累计金额
        BigDecimal totalMoney = BigDecimal.ZERO;
        List<OrderItem> orderItems = new ArrayList<>();
        List<String> orderedSkuIds = new ArrayList<>();

        for (OrderSubmitDTO.GoodsDTO goodsDTO : orderSubmitDTO.getGoods()) {
            if (goodsDTO.getCount() == null || goodsDTO.getCount() <= 0) {
                throw new BaseException("购买数量不合法");
            }
            GoodsSku sku = goodsSkuMapper.selectById(goodsDTO.getSkuId());
            if (sku == null) {
                throw new BaseException(MessageConstant.SKU_NOT_FOUND);
            }
            Goods goods = goodsMapper.selectById(sku.getGoodsId());

            // 条件更新扣库存：只有 inventory >= count 才扣，防止超卖
            int rows = goodsSkuMapper.update(null, new LambdaUpdateWrapper<GoodsSku>()
                    .eq(GoodsSku::getId, goodsDTO.getSkuId())
                    .ge(GoodsSku::getInventory, goodsDTO.getCount())
                    .setSql("inventory = inventory - " + goodsDTO.getCount()));
            if (rows == 0) {
                throw new BaseException(MessageConstant.SKU_STOCK_NOT_ENOUGH);
            }

            BigDecimal price = sku.getPrice();
            BigDecimal count = new BigDecimal(goodsDTO.getCount());

            OrderItem item = new OrderItem();
            item.setOrderId(orderId);
            item.setGoodsId(sku.getGoodsId());
            item.setSkuId(sku.getId());
            item.setName(goods != null ? goods.getName() : "");
            item.setPicture(goods != null ? goods.getPicture() : "");
            item.setAttrsText(buildAttrsText(sku.getSpecs()));
            item.setPrice(price);
            item.setPayPrice(price);
            item.setCount(goodsDTO.getCount());
            item.setTotalPrice(price.multiply(count));
            item.setTotalPayPrice(price.multiply(count));
            orderItems.add(item);
            orderedSkuIds.add(sku.getId());

            totalMoney = totalMoney.add(item.getTotalPrice());
        }

        // 4. 保存订单
        BigDecimal postFee = BigDecimal.ZERO;
        Orders orders = new Orders();
        orders.setId(orderId);
        orders.setUserId(userId);
        orders.setOrderState(OrderStatusConstant.UNPAY);
        orders.setTotalMoney(totalMoney);
        orders.setPostFee(postFee);
        orders.setPayMoney(totalMoney.add(postFee));
        orders.setPayType(orderSubmitDTO.getPayType());
        orders.setPayChannel(orderSubmitDTO.getPayChannel());
        orders.setDeliveryTimeType(orderSubmitDTO.getDeliveryTimeType());
        orders.setBuyerMessage(orderSubmitDTO.getBuyerMessage());
        orders.setAddressSnapshot(buildAddressSnapshot(address));
        ordersMapper.insert(orders);

        // 5. 批量保存订单项
        for (OrderItem item : orderItems) {
            orderItemMapper.insert(item);
        }

        // 6. 下单成功后删除购物车中已下单的商品
        cartService.delete(orderedSkuIds);

        return orderId;
    }

    @Override
    public PageResult<OrderVO> list(Integer orderState, Integer page, Integer pageSize) {
        Long userId = BaseContext.getCurrentId();

        LambdaQueryWrapper<Orders> wrapper = new LambdaQueryWrapper<Orders>()
                .eq(Orders::getUserId, userId)
                .orderByDesc(Orders::getCreateTime);
        if (orderState != null && orderState != OrderStatusConstant.ALL) {
            wrapper.eq(Orders::getOrderState, orderState);
        }

        Page<Orders> orderPage = new Page<>(page, pageSize);
        Page<Orders> result = ordersMapper.selectPage(orderPage, wrapper);

        List<OrderVO> items = result.getRecords().stream()
                .map(this::toOrderVO)
                .collect(Collectors.toList());
        return new PageResult<>(items, result.getTotal(), page, pageSize);
    }

    @Override
    public OrderDetailVO detail(String id) {
        Orders order = getOwnOrder(id);
        List<OrderItem> orderItems = orderItemMapper.selectList(new LambdaQueryWrapper<OrderItem>()
                .eq(OrderItem::getOrderId, id));

        Map<String, String> address = parseAddressSnapshot(order.getAddressSnapshot());

        return OrderDetailVO.builder()
                .id(order.getId())
                .createTime(order.getCreateTime())
                .orderState(order.getOrderState())
                .countdown(computeCountdownSeconds(order))
                .payMoney(order.getPayMoney())
                .totalMoney(order.getTotalMoney())
                .postFee(order.getPostFee())
                .payType(order.getPayType())
                .deliveryTimeType(order.getDeliveryTimeType())
                .buyerMessage(order.getBuyerMessage())
                .receiver(address.get("receiver"))
                .contact(address.get("contact"))
                .fullLocation(address.get("fullLocation"))
                .address(address.get("address"))
                .skus(toOrderItemVOs(orderItems))
                .build();
    }

    @Override
    @Transactional
    public void cancel(String id) {
        Orders order = getOwnOrder(id);
        // 只有待付款订单可以取消
        if (order.getOrderState() != OrderStatusConstant.UNPAY) {
            throw new BaseException(MessageConstant.ORDER_STATUS_ERROR);
        }

        // 归还库存
        List<OrderItem> items = orderItemMapper.selectList(new LambdaQueryWrapper<OrderItem>()
                .eq(OrderItem::getOrderId, id));
        for (OrderItem item : items) {
            goodsSkuMapper.update(null, new LambdaUpdateWrapper<GoodsSku>()
                    .eq(GoodsSku::getId, item.getSkuId())
                    .setSql("inventory = inventory + " + item.getCount()));
        }

        order.setOrderState(OrderStatusConstant.CANCELED);
        ordersMapper.updateById(order);
    }

    @Override
    @Transactional
    public void delete(String id) {
        Orders order = getOwnOrder(id);
        // 只允许删除已完成或已取消订单
        if (order.getOrderState() != OrderStatusConstant.COMPLETED
                && order.getOrderState() != OrderStatusConstant.CANCELED) {
            throw new BaseException(MessageConstant.ORDER_STATUS_ERROR);
        }
        ordersMapper.deleteById(id);
        orderItemMapper.delete(new LambdaQueryWrapper<OrderItem>().eq(OrderItem::getOrderId, id));
    }

    @Override
    public void pay(String id) {
        // 支付是浏览器直接跳转（无登录态），因此不校验当前用户，按订单号直接查询
        Orders order = ordersMapper.selectById(id);
        if (order == null) {
            throw new BaseException(MessageConstant.ORDER_NOT_FOUND);
        }
        if (order.getOrderState() != OrderStatusConstant.UNPAY) {
            throw new BaseException(MessageConstant.ORDER_STATUS_ERROR);
        }
        order.setOrderState(OrderStatusConstant.WAIT_DELIVER);
        order.setPayTime(LocalDateTime.now());
        ordersMapper.updateById(order);
    }

    // ==================== 私有辅助方法 ====================

    private OrderPreVO buildOrderPre(List<CartVO> cartList) {
        List<OrderPreVO.GoodsVO> goods = new ArrayList<>();
        int goodsCount = 0;
        BigDecimal totalPrice = BigDecimal.ZERO;

        for (CartVO cart : cartList) {
            BigDecimal count = new BigDecimal(cart.getCount());
            BigDecimal itemTotal = cart.getPrice().multiply(count);

            goods.add(OrderPreVO.GoodsVO.builder()
                    .id(cart.getId())
                    .skuId(cart.getSkuId())
                    .name(cart.getName())
                    .picture(cart.getPicture())
                    .price(cart.getPrice())
                    .payPrice(cart.getPrice())
                    .count(cart.getCount())
                    .attrsText(cart.getAttrsText())
                    .totalPrice(itemTotal)
                    .totalPayPrice(itemTotal)
                    .build());

            goodsCount += cart.getCount();
            totalPrice = totalPrice.add(itemTotal);
        }

        BigDecimal postFee = BigDecimal.ZERO;
        OrderPreVO.SummaryVO summary = OrderPreVO.SummaryVO.builder()
                .goodsCount(goodsCount)
                .totalPrice(totalPrice)
                .postFee(postFee)
                .totalPayPrice(totalPrice.add(postFee))
                .build();

        return OrderPreVO.builder()
                .goods(goods)
                .summary(summary)
                .userAddresses(listAddresses())
                .build();
    }

    private List<UserAddress> listAddresses() {
        return userAddressMapper.selectList(new LambdaQueryWrapper<UserAddress>()
                .eq(UserAddress::getUserId, BaseContext.getCurrentId())
                .orderByAsc(UserAddress::getIsDefault)
                .orderByDesc(UserAddress::getCreateTime));
    }

    private Orders getOwnOrder(String id) {
        Orders order = ordersMapper.selectById(id);
        if (order == null || !order.getUserId().equals(BaseContext.getCurrentId())) {
            throw new BaseException(MessageConstant.ORDER_NOT_FOUND);
        }
        return order;
    }

    private OrderVO toOrderVO(Orders order) {
        List<OrderItem> items = orderItemMapper.selectList(new LambdaQueryWrapper<OrderItem>()
                .eq(OrderItem::getOrderId, order.getId()));
        boolean unpaid = order.getOrderState() != null && order.getOrderState() == OrderStatusConstant.UNPAY;
        return OrderVO.builder()
                .id(order.getId())
                .createTime(order.getCreateTime())
                .orderState(order.getOrderState())
                .countdown(unpaid ? formatCountdown(computeCountdownSeconds(order)) : null)
                .skus(toOrderItemVOs(items))
                .payMoney(order.getPayMoney())
                .postFee(order.getPostFee())
                .totalMoney(order.getTotalMoney())
                .build();
    }

    private List<OrderItemVO> toOrderItemVOs(List<OrderItem> items) {
        return items.stream().map(item -> OrderItemVO.builder()
                .id(item.getId())
                .image(item.getPicture())
                .name(item.getName())
                .attrsText(item.getAttrsText())
                .realPay(item.getPayPrice())
                .quantity(item.getCount())
                .build()).collect(Collectors.toList());
    }

    private long computeCountdownSeconds(Orders order) {
        if (order.getOrderState() != null && order.getOrderState() == OrderStatusConstant.UNPAY) {
            LocalDateTime deadline = order.getCreateTime().plusMinutes(PAY_TIMEOUT_MINUTES);
            return Math.max(Duration.between(LocalDateTime.now(), deadline).getSeconds(), 0);
        }
        return 0;
    }

    private String formatCountdown(long seconds) {
        long hours = seconds / 3600;
        long minutes = (seconds % 3600) / 60;
        long secs = seconds % 60;
        return String.format("%02d:%02d:%02d", hours, minutes, secs);
    }

    private String generateOrderId() {
        return LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS"))
                + String.format("%03d", new Random().nextInt(1000));
    }

    private String buildAddressSnapshot(UserAddress address) {
        Map<String, String> map = new HashMap<>();
        map.put("receiver", address.getReceiver());
        map.put("contact", address.getContact());
        map.put("fullLocation", address.getFullLocation());
        map.put("address", address.getAddress());
        try {
            return objectMapper.writeValueAsString(map);
        } catch (Exception e) {
            return "{}";
        }
    }

    private Map<String, String> parseAddressSnapshot(String json) {
        try {
            if (json == null || json.isEmpty()) {
                return Map.of();
            }
            return objectMapper.readValue(json, new TypeReference<Map<String, String>>() {
            });
        } catch (Exception e) {
            return Map.of();
        }
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
}