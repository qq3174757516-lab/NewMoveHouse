package com.newmovehouse.controller;

import com.newmovehouse.common.ApiResponse;
import com.newmovehouse.dto.AuthDtos;
import com.newmovehouse.entity.Entities;
import com.newmovehouse.service.OrderService;
import com.newmovehouse.service.ProfileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * 司机端：资料、接单大厅、订单操作、位置上报、收入、投诉与资质文件。
 */
@RestController
@RequestMapping("/api/driver")
public class DriverController {
    @Autowired
    private ProfileService profileService;
    @Autowired
    private OrderService orderService;

    /** 当前司机资料 */
    @GetMapping("/me")
    public ApiResponse<Object> me() {
        return ApiResponse.ok(profileService.me());
    }

    /** 更新司机资料 */
    @PutMapping("/profile")
    public ApiResponse<Void> profile(@Valid @RequestBody AuthDtos.UpdateDriverProfileReq req) {
        profileService.updateDriverProfile(req);
        return ApiResponse.ok();
    }

    /** 接单大厅 */
    @GetMapping("/hall")
    public ApiResponse<List<Map<String, Object>>> hall() {
        return ApiResponse.ok(orderService.hall());
    }

    /** 抢单 */
    @PostMapping("/orders/{id}/accept")
    public ApiResponse<Map<String, Object>> accept(@PathVariable Long id) {
        return ApiResponse.ok(orderService.accept(id));
    }

    /** 我的订单 */
    @GetMapping("/orders")
    public ApiResponse<List<Map<String, Object>>> orders(@RequestParam(required = false) String status) {
        return ApiResponse.ok(orderService.driverOrders(status));
    }

    /** 订单详情 */
    @GetMapping("/orders/{id}")
    public ApiResponse<Map<String, Object>> detail(@PathVariable Long id) {
        return ApiResponse.ok(orderService.detail(id));
    }

    /** 推进订单状态（到达、开始搬运、完成等） */
    @PostMapping("/orders/{id}/status")
    public ApiResponse<Map<String, Object>> status(@PathVariable Long id, @RequestBody Map<String, String> req) {
        return ApiResponse.ok(orderService.nextStatus(id, req.get("action")));
    }

    /** 上报当前经纬度 */
    @PostMapping("/location")
    public ApiResponse<Void> location(@RequestBody Map<String, Object> req) {
        profileService.reportLocation(new BigDecimal(String.valueOf(req.get("lng"))), new BigDecimal(String.valueOf(req.get("lat"))));
        return ApiResponse.ok();
    }

    /** 收入汇总 */
    @GetMapping("/income")
    public ApiResponse<Map<String, Object>> income() {
        return ApiResponse.ok(profileService.driverIncome());
    }

    /** 收入看板（含趋势） */
    @GetMapping("/income/dashboard")
    public ApiResponse<Map<String, Object>> incomeDashboard() {
        return ApiResponse.ok(profileService.driverIncomeDashboard());
    }

    /** 与我相关的投诉 */
    @GetMapping("/complaints")
    public ApiResponse<List<Map<String, Object>>> complaints() {
        return ApiResponse.ok(orderService.driverComplaints());
    }

    /** 已上传资质列表 */
    @GetMapping("/documents")
    public ApiResponse<List<Entities.DriverDocument>> documents() {
        return ApiResponse.ok(profileService.driverDocuments());
    }

    /** 登记一条资质文件（URL 由上传接口返回） */
    @PostMapping("/documents")
    public ApiResponse<Void> uploadDocument(@RequestBody Map<String, String> req) {
        profileService.addDriverDocument(req.get("docType"), req.get("fileUrl"), req.get("originalName"));
        return ApiResponse.ok();
    }
}
