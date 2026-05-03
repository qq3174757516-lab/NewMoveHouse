package com.newmovehouse.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * 将本地上传目录映射为 {@code /files/**} 静态资源，与 {@code upload.base-dir} 一致。
 */
@Configuration
public class StaticWebConfig implements WebMvcConfigurer {

    @Value("${upload.base-dir:uploads}")
    private String uploadBaseDir;

    /** 注册 {@code /files/**} → 配置的本地目录 */
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        String path = uploadBaseDir.replace("\\", "/");
        if (!path.endsWith("/")) {
            path = path + "/";
        }
        registry.addResourceHandler("/files/**")
                .addResourceLocations("file:" + path);
    }
}
