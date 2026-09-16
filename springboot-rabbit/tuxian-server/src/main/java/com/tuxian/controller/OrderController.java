package com.tuxian.controller;

import com.tuxian.common.result.PageResult;
import com.tuxian.common.result.Result;
import com.tuxian.pojo.dto.OrderSubmitDTO;
import com.tuxian.pojo.vo.OrderDetailVO;
import com.tuxian.pojo.vo.OrderPreVO;
import com.tuxian.pojo.vo.OrderVO;
import com.tuxian.service.OrderService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * 订单相关接口。
 */
@RestController
@RequestMapping("/member/order")
@Api(tags = "订单相关接口")
public class OrderController {

    @Autowired
    private OrderService orderService;

    @GetMapping("/pre")
    @ApiOperation("获取预结算订单（购物车结算）")
    public Result<OrderPreVO> pre() {
        return Result.success(orderService.pre());
    }

    @GetMapping("/pre/now")
    @ApiOperation("获取立即购买预结算订单")
    public Result<OrderPreVO> preNow(@RequestParam String skuId,
                                     @RequestParam Integer count,
                                     @RequestParam(required = false) Long addressId) {
        return Result.success(orderService.preNow(skuId, count));
    }

    @PostMapping
    @ApiOperation("提交订单")
    public Result<Map<String, String>> submit(@RequestBody @Validated OrderSubmitDTO orderSubmitDTO) {
        String orderId = orderService.submit(orderSubmitDTO);
        return Result.success(Map.of("id", orderId));
    }

    @GetMapping
    @ApiOperation("获取订单列表")
    public Result<PageResult<OrderVO>> list(@RequestParam(defaultValue = "0") Integer orderState,
                                            @RequestParam(defaultValue = "1") Integer page,
                                            @RequestParam(defaultValue = "10") Integer pageSize) {
        return Result.success(orderService.list(orderState, page, pageSize));
    }

    @GetMapping("/{id}")
    @ApiOperation("获取订单详情")
    public Result<OrderDetailVO> detail(@PathVariable String id) {
        return Result.success(orderService.detail(id));
    }

    @PutMapping("/{id}/cancel")
    @ApiOperation("取消订单")
    public Result<Void> cancel(@PathVariable String id) {
        orderService.cancel(id);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    @ApiOperation("删除订单")
    public Result<Void> delete(@PathVariable String id) {
        orderService.delete(id);
        return Result.success();
    }
}
