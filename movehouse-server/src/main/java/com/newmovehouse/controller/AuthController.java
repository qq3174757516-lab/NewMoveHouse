package com.newmovehouse.controller;

import com.newmovehouse.common.ApiResponse;
import com.newmovehouse.dto.AuthDtos;
import com.newmovehouse.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.util.Map;

/**
 * 登录、注册、登出等认证相关 HTTP 接口。
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {
    @Autowired
    private AuthService authService;

    /** 用户/司机/管理员登录，返回 token 与 profile */
    @PostMapping("/login")
    public ApiResponse<Map<String, Object>> login(@Valid @RequestBody AuthDtos.LoginReq req) {
        return ApiResponse.ok(authService.login(req));
    }

    /** 用户注册 */
    @PostMapping("/user/register")
    public ApiResponse<Map<String, Object>> registerUser(@Valid @RequestBody AuthDtos.RegisterUserReq req) {
        return ApiResponse.ok(authService.registerUser(req));
    }

    /** 司机注册 */
    @PostMapping("/driver/register")
    public ApiResponse<Map<String, Object>> registerDriver(@Valid @RequestBody AuthDtos.RegisterDriverReq req) {
        return ApiResponse.ok(authService.registerDriver(req));
    }

    /** 登出：token 加入黑名单 */
    @PostMapping("/logout")
    public ApiResponse<Void> logout(HttpServletRequest request) {
        authService.logout(request.getHeader("Authorization"));
        return ApiResponse.ok();
    }
}
