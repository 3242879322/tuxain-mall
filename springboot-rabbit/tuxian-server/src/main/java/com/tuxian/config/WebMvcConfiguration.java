package com.tuxian.config;

import com.tuxian.common.json.JacksonObjectMapper;
import com.tuxian.interceptor.JwtTokenAdminInterceptor;
import com.tuxian.interceptor.JwtTokenUserInterceptor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;

/**
 * Web MVC 配置类。
 * <p>
 * 1. 注册 JWT 拦截器（用户端 /member/**、管理端 /admin/**）；
 * 2. 配置跨域（前端 Vite 运行在 5173 端口，后端 8080 端口）；
 * 3. 扩展消息转换器，使用自定义的 JacksonObjectMapper 序列化 LocalDateTime 等。
 */
@Slf4j
@Configuration
public class WebMvcConfiguration implements WebMvcConfigurer {

    @Autowired
    private JwtTokenUserInterceptor jwtTokenUserInterceptor;

    @Autowired
    private JwtTokenAdminInterceptor jwtTokenAdminInterceptor;

    /**
     * 注册拦截器。
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // 用户端：/member/** 下所有接口都需要登录
        registry.addInterceptor(jwtTokenUserInterceptor)
                .addPathPatterns("/member/**");

        // 管理端：/admin/** 下所有接口需要登录，排除登录接口本身
        registry.addInterceptor(jwtTokenAdminInterceptor)
                .addPathPatterns("/admin/**")
                .excludePathPatterns("/admin/login");
    }

    /**
     * 跨域配置：前端 dev 服务（如 http://localhost:5173）直接请求后端。
     */
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOriginPatterns("*")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true)
                .maxAge(3600);
    }

    /**
     * 扩展消息转换器：使用自定义 ObjectMapper 处理 LocalDateTime 的序列化格式。
     */
    @Override
    public void extendMessageConverters(List<HttpMessageConverter<?>> converters) {
        MappingJackson2HttpMessageConverter converter = new MappingJackson2HttpMessageConverter();
        converter.setObjectMapper(new JacksonObjectMapper());
        // 放在最前面，优先使用
        converters.add(0, converter);
    }
}