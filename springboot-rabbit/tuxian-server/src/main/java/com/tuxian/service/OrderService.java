package com.tuxian.service;

import com.tuxian.common.result.PageResult;
import com.tuxian.pojo.dto.OrderSubmitDTO;
import com.tuxian.pojo.vo.OrderDetailVO;
import com.tuxian.pojo.vo.OrderPreVO;
import com.tuxian.pojo.vo.OrderVO;

public interface OrderService {

    /** 预结算（购物车结算） */
    OrderPreVO pre();

    /** 预结算（立即购买） */
    OrderPreVO preNow(String skuId, Integer count);

    /** 提交订单，返回订单号 */
    String submit(OrderSubmitDTO orderSubmitDTO);

    /** 订单列表 */
    PageResult<OrderVO> list(Integer orderState, Integer page, Integer pageSize);

    /** 订单详情 */
    OrderDetailVO detail(String id);

    /** 取消订单 */
    void cancel(String id);

    /** 删除订单（逻辑删除） */
    void delete(String id);

    /** 模拟支付 */
    void pay(String id);
}