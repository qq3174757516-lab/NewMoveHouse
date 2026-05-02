package com.newmovehouse.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * 将本地上传目录映射为 {@code /files/**} 静态资源，供前端访问上传文件 URL。
 */
@Configuration
public class StaticWebConfig implements WebMvcConfigurer {

    /** 注册 {@code /files/**} → {@code file:uploads/} */
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/files/**")
                .addResourceLocations("file:uploads/");
    }
}
