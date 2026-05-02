package com.newmovehouse.service;

import com.newmovehouse.common.BizException;
import com.newmovehouse.dto.AuthDtos;
import com.newmovehouse.mapper.AppMapper;
import com.newmovehouse.util.JwtUtil;
import com.newmovehouse.util.PasswordUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Service
public class AuthService {
    @Autowired
    private AppMapper mapper;
    @Autowired
    private JwtUtil jwtUtil;
    @Autowired
    private StringRedisTemplate redisTemplate;

    public Map<String, Object> login(AuthDtos.LoginReq req) {
        Map<String, Object> account;
        String auditStatus = null;
        if ("USER".equals(req.role)) {
            account = mapper.findUserByUsername(req.username);
            if (account != null && Integer.valueOf(String.valueOf(account.get("status"))) == 0) {
                throw new BizException("账号已禁用");
            }
        } else if ("DRIVER".equals(req.role)) {
            account = mapper.findDriverByUsername(req.username);
            if (account != null) {
                auditStatus = String.valueOf(account.get("auditStatus"));
                if (Integer.valueOf(String.valueOf(account.get("status"))) == 0 || "DISABLED".equals(auditStatus)) {
                    throw new BizException("司机账号已停用");
                }
            }
        } else if ("ADMIN".equals(req.role)) {
            account = mapper.findAdminByUsername(req.username);
        } else {
            throw new BizException("角色错误");
        }
        if (account == null || !PasswordUtil.matches(req.password, String.valueOf(account.get("passwordHash")))) {
            throw new BizException("用户名或密码错误");
        }
        Long id = Long.valueOf(String.valueOf(account.get("id")));
        String token = jwtUtil.create(id, req.role, req.username, auditStatus);
        Map<String, Object> res = new HashMap<>();
        res.put("token", token);
        res.put("profile", account);
        res.put("role", req.role);
        res.put("auditStatus", auditStatus);
        return res;
    }

    public Map<String, Object> registerUser(AuthDtos.RegisterUserReq req) {
        if (mapper.findUserByUsername(req.username) != null) {
            throw new BizException("用户名已存在");
        }
        mapper.insertUser(req, PasswordUtil.hash(req.password));
        return login(asLogin(req.username, req.password, "USER"));
    }

    public Map<String, Object> registerDriver(AuthDtos.RegisterDriverReq req) {
        if (mapper.findDriverByUsername(req.username) != null) {
            throw new BizException("用户名已存在");
        }
        mapper.insertDriver(req, PasswordUtil.hash(req.password));
        return login(asLogin(req.username, req.password, "DRIVER"));
    }

    public void logout(String header) {
        if (header == null || !header.startsWith("Bearer ")) {
            return;
        }
        String token = header.substring(7);
        redisTemplate.opsForValue().set("jwt:blacklist:" + token, "1", jwtUtil.getExpireSeconds(), TimeUnit.SECONDS);
    }

    private AuthDtos.LoginReq asLogin(String username, String password, String role) {
        AuthDtos.LoginReq req = new AuthDtos.LoginReq();
        req.username = username;
        req.password = password;
        req.role = role;
        return req;
    }
}

