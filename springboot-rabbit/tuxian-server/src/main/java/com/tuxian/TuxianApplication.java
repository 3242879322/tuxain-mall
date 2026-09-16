package com.tuxian;

import lombok.extern.slf4j.Slf4j;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 小兔鲜商城 启动类。
 */
@Slf4j
@SpringBootApplication
@MapperScan("com.tuxian.mapper")
public class TuxianApplication {

    public static void main(String[] args) {
        SpringApplication.run(TuxianApplication.class, args);
        log.info("小兔鲜商城后端启动成功，接口文档：http://localhost:8080/doc.html");
    }
}