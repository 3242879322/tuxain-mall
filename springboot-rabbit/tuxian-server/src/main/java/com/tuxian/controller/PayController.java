package com.tuxian.controller;

import com.tuxian.service.OrderService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * 支付相关接口（模拟支付）。
 * <p>
 * 前端跳转到 /pay/aliPay?orderId=xxx&redirect=xxx，本接口把订单置为已支付后
 * 302 重定向回 redirect 地址，并携带 orderId 与 payResult=true。
 */
@RestController
@Api(tags = "支付相关接口")
public class PayController {

    @Autowired
    private OrderService orderService;

    @GetMapping("/pay/aliPay")
    @ApiOperation("模拟支付宝支付（跳转）")
    public void pay(@RequestParam String orderId,
                    @RequestParam String redirect,
                    HttpServletResponse response) throws IOException {
        orderService.pay(orderId);

        String separator = redirect.contains("?") ? "&" : "?";
        response.sendRedirect(redirect + separator + "orderId=" + orderId + "&payResult=true");
    }
}
