package com.newmovehouse.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * JWT 签发与解析，使用 HS256；密钥长度不足时自动填充至 32 字节。
 */
@Component
public class JwtUtil {
    @Value("${jwt.secret}")
    private String secret;
    @Value("${jwt.expire-seconds:86400}")
    private long expireSeconds;

    /** 由配置 secret 派生 HMAC 密钥 */
    private SecretKey key() {
        byte[] bytes = secret.getBytes(StandardCharsets.UTF_8);
        if (bytes.length < 32) {
            byte[] padded = new byte[32];
            System.arraycopy(bytes, 0, padded, 0, bytes.length);
            bytes = padded;
        }
        return Keys.hmacShaKeyFor(bytes);
    }

    /**
     * 签发访问令牌。
     *
     * @param id           用户主键
     * @param role         角色
     * @param username     用户名
     * @param auditStatus  司机审核状态，可 null
     * @return compact JWT
     */
    public String create(Long id, String role, String username, String auditStatus) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("id", id);
        claims.put("role", role);
        claims.put("username", username);
        if (auditStatus != null) {
            claims.put("auditStatus", auditStatus);
        }
        Date now = new Date();
        return Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(now)
                .setExpiration(new Date(now.getTime() + expireSeconds * 1000))
                .signWith(key(), SignatureAlgorithm.HS256)
                .compact();
    }

    /** 解析 JWT 载荷为 Map */
    public Map<String, Object> parse(String token) {
        Claims claims = Jwts.parserBuilder().setSigningKey(key()).build().parseClaimsJws(token).getBody();
        return new HashMap<>(claims);
    }

    /** 配置的过期秒数，供登出黑名单 TTL 使用 */
    public long getExpireSeconds() {
        return expireSeconds;
    }
}
