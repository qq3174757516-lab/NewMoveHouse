package com.newmovehouse.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.newmovehouse.common.CurrentUser;
import com.newmovehouse.common.UserContext;
import com.newmovehouse.mapper.AppMapper;
import com.newmovehouse.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Web MVC 配置：注册 JWT 鉴权拦截器与全局 CORS。
 */
@Component
public class WebConfig implements WebMvcConfigurer {
    @Autowired
    private AuthInterceptor authInterceptor;

    /** 将 {@link AuthInterceptor} 应用到 {@code /api/**}，并排除登录、公开估价等路径 */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(authInterceptor)
                .addPathPatterns("/api/**")
                .excludePathPatterns(
                        "/api/auth/**",
                        "/api/common/vehicle-types",
                        "/api/common/estimate",
                        "/api/common/map/**",
                        "/api/common/map-web-config",
                        "/api/common/amap/**",
                        "/api/common/announcements/**"
                );
    }

    /** 允许浏览器跨域访问（含携带 Cookie 场景） */
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOriginPatterns("*")
                .allowedMethods("*")
                .allowedHeaders("*")
                .allowCredentials(true);
    }
}

/**
 * JWT 解析、黑名单校验、角色路由守卫，并在请求线程中设置 {@link UserContext}。
 */
@Component
class AuthInterceptor implements HandlerInterceptor {
    @Autowired
    private JwtUtil jwtUtil;
    @Autowired
    private StringRedisTemplate redisTemplate;
    @Autowired
    private AppMapper mapper;
    @Autowired
    private ObjectMapper objectMapper;

    /**
     * 预处理方法：校验 Authorization，写入 {@link UserContext}。
     *
     * @return false 表示已写错误响应并中断链路
     */
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }
        String header = request.getHeader("Authorization");
        if (header == null || !header.startsWith("Bearer ")) {
            writeError(response, 401, "未登录或登录已过期");
            return false;
        }
        String token = header.substring(7);
        if (Boolean.TRUE.equals(redisTemplate.hasKey("jwt:blacklist:" + token))) {
            writeError(response, 401, "登录已失效，请重新登录");
            return false;
        }
        Map<String, Object> claims;
        try {
            claims = jwtUtil.parse(token);
        } catch (Exception e) {
            writeError(response, 401, "Token无效，请重新登录");
            return false;
        }
        String role = String.valueOf(claims.get("role"));
        Long id = Long.valueOf(String.valueOf(claims.get("id")));
        String auditStatus = claims.get("auditStatus") == null ? null : String.valueOf(claims.get("auditStatus"));
        if ("DRIVER".equals(role)) {
            auditStatus = currentDriverAuditStatus(id, auditStatus);
        }
        CurrentUser currentUser = new CurrentUser(
                id,
                role,
                String.valueOf(claims.get("username")),
                auditStatus
        );
        String uri = request.getRequestURI();
        if ((uri.startsWith("/api/admin") && !"ADMIN".equals(currentUser.role))
                || (uri.startsWith("/api/user") && !"USER".equals(currentUser.role))
                || (uri.startsWith("/api/driver") && !"DRIVER".equals(currentUser.role))) {
            writeError(response, 403, "无权限访问");
            return false;
        }
        UserContext.set(currentUser);
        return true;
    }

    /** 写入 JSON 格式错误体 */
    private void writeError(HttpServletResponse response, int status, String message) {
        response.setStatus(status);
        response.setCharacterEncoding("UTF-8");
        response.setContentType("application/json;charset=UTF-8");
        try {
            response.getWriter().write(objectMapper.writeValueAsString(new com.newmovehouse.common.ApiResponse<>(status, message, null)));
        } catch (IOException ignored) {
        }
    }

    /** 从 Redis 缓存或数据库读取司机最新审核状态，保证 token 与资料一致 */
    private String currentDriverAuditStatus(Long driverId, String fallback) {
        String key = "driver:profile:" + driverId;
        try {
            String cached = redisTemplate.opsForValue().get(key);
            if (cached != null) {
                Map map = objectMapper.readValue(cached, Map.class);
                Object status = map.get("auditStatus");
                if (status != null) {
                    return String.valueOf(status);
                }
            }
            Map<String, Object> driver = mapper.findDriverById(driverId);
            if (driver != null) {
                redisTemplate.opsForValue().set(key, objectMapper.writeValueAsString(driver), 30, TimeUnit.MINUTES);
                Object status = driver.get("auditStatus");
                return status == null ? fallback : String.valueOf(status);
            }
        } catch (Exception ignored) {
        }
        return fallback;
    }

    /** 请求完成后清理 ThreadLocal */
    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        UserContext.clear();
    }
}
