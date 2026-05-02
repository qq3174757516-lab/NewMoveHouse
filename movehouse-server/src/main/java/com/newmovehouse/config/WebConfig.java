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
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Component
public class WebConfig implements WebMvcConfigurer {
    @Autowired
    private AuthInterceptor authInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(authInterceptor)
                .addPathPatterns("/api/**")
                .excludePathPatterns("/api/auth/**", "/api/common/vehicle-types", "/api/common/estimate", "/api/common/map/**");
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOriginPatterns("*")
                .allowedMethods("*")
                .allowedHeaders("*")
                .allowCredentials(true);
    }
}

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

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }
        String header = request.getHeader("Authorization");
        if (header == null || !header.startsWith("Bearer ")) {
            response.setStatus(401);
            return false;
        }
        String token = header.substring(7);
        if (Boolean.TRUE.equals(redisTemplate.hasKey("jwt:blacklist:" + token))) {
            response.setStatus(401);
            return false;
        }
        Map<String, Object> claims = jwtUtil.parse(token);
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
            response.setStatus(403);
            return false;
        }
        UserContext.set(currentUser);
        return true;
    }

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

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        UserContext.clear();
    }
}
