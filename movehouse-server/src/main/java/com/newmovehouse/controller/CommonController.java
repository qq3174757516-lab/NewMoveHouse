package com.newmovehouse.controller;

import com.newmovehouse.common.ApiResponse;
import com.newmovehouse.dto.OrderDtos;
import com.newmovehouse.entity.Entities;
import com.newmovehouse.service.PricingService;
import com.newmovehouse.service.ProfileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 无需登录的公共接口：车型、估价、司机位置查询、高德 Web Key 下发等。
 */
@RestController
@RequestMapping("/api/common")
public class CommonController {
    @Autowired
    private PricingService pricingService;
    @Autowired
    private ProfileService profileService;

    @Value("${amap.web-key:}")
    private String amapWebKey;
    @Value("${amap.web-security-js-code:}")
    private String amapWebSecurityJsCode;

    /** 启用中的车型及价格 */
    @GetMapping("/vehicle-types")
    public ApiResponse<List<Entities.VehicleType>> vehicleTypes() {
        return ApiResponse.ok(pricingService.vehicleTypes());
    }

    /** 根据起终点与车型估算费用 */
    @PostMapping("/estimate")
    public ApiResponse<Map<String, Object>> estimate(@Valid @RequestBody OrderDtos.EstimateReq req) {
        return ApiResponse.ok(pricingService.estimate(req));
    }

    /** 查询司机最近一次上报的坐标（Redis） */
    @GetMapping("/driver-location/{driverId}")
    public ApiResponse<Object> driverLocation(@PathVariable Long driverId) {
        return ApiResponse.ok(profileService.driverLocation(driverId));
    }

    /**
     * 前端高德 JSAPI 2.0 使用：Web Key + 安全密钥（配合服务端 application.yml，避免仅依赖前端 .env）
     * 参考：https://lbs.amap.com/api/javascript-api-v2/getting-started
     */
    @GetMapping("/map-web-config")
    public ApiResponse<Map<String, String>> mapWebConfig() {
        Map<String, String> m = new HashMap<>();
        m.put("key", amapWebKey == null ? "" : amapWebKey);
        m.put("securityJsCode", amapWebSecurityJsCode == null ? "" : amapWebSecurityJsCode);
        return ApiResponse.ok(m);
    }
}
