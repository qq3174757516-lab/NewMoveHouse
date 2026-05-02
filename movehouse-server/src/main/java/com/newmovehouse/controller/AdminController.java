package com.newmovehouse.controller;

import com.newmovehouse.common.ApiResponse;
import com.newmovehouse.common.UserContext;
import com.newmovehouse.dto.AdminDtos;
import com.newmovehouse.dto.OrderDtos;
import com.newmovehouse.entity.Entities;
import com.newmovehouse.mapper.AppMapper;
import com.newmovehouse.service.AdminService;
import com.newmovehouse.service.OrderService;
import com.newmovehouse.service.PricingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
public class AdminController {
    @Autowired
    private AdminService adminService;
    @Autowired
    private PricingService pricingService;
    @Autowired
    private OrderService orderService;
    @Autowired
    private AppMapper mapper;

    @GetMapping("/dashboard")
    public ApiResponse<Map<String, Object>> dashboard() {
        return ApiResponse.ok(adminService.dashboard());
    }

    @GetMapping("/users")
    public ApiResponse<List<Map<String, Object>>> users() {
        return ApiResponse.ok(adminService.users());
    }

    @PostMapping("/users/{id}/status")
    public ApiResponse<Void> userStatus(@PathVariable Long id, @RequestBody Map<String, Object> req) {
        adminService.userStatus(id, Integer.valueOf(String.valueOf(req.get("status"))));
        return ApiResponse.ok();
    }

    @GetMapping("/drivers")
    public ApiResponse<List<Map<String, Object>>> drivers(@RequestParam(required = false) String auditStatus) {
        return ApiResponse.ok(adminService.drivers(auditStatus));
    }

    @PostMapping("/drivers/audit")
    public ApiResponse<Void> auditDriver(@Valid @RequestBody AdminDtos.AuditDriverReq req) {
        adminService.auditDriver(req);
        return ApiResponse.ok();
    }

    @PutMapping("/driver/{driverId}/audit")
    public ApiResponse<Void> auditDriverCompat(@PathVariable Long driverId, @RequestBody Map<String, String> req) {
        AdminDtos.AuditDriverReq body = new AdminDtos.AuditDriverReq();
        body.driverId = driverId;
        body.status = req.get("status");
        body.reason = req.get("reason");
        adminService.auditDriver(body);
        return ApiResponse.ok();
    }

    @GetMapping("/orders")
    public ApiResponse<List<Map<String, Object>>> orders(@RequestParam(required = false) String status) {
        return ApiResponse.ok(mapper.listAdminOrders(status));
    }

    @GetMapping("/orders/{id}")
    public ApiResponse<Map<String, Object>> detail(@PathVariable Long id) {
        return ApiResponse.ok(orderService.detail(id));
    }

    @PostMapping("/orders/{id}/cancel")
    public ApiResponse<Map<String, Object>> cancel(@PathVariable Long id, @RequestBody(required = false) OrderDtos.CancelReq req) {
        return ApiResponse.ok(orderService.cancel(id, req == null ? "管理员取消" : req.reason, "ADMIN", UserContext.id()));
    }

    @GetMapping("/vehicle-types")
    public ApiResponse<List<Entities.VehicleType>> vehicleTypes() {
        return ApiResponse.ok(mapper.listAdminVehicleTypes());
    }

    @PostMapping("/vehicle-types")
    public ApiResponse<Object> saveVehicle(@Valid @RequestBody AdminDtos.VehicleTypeReq req) {
        return ApiResponse.ok(adminService.saveVehicle(req));
    }

    @DeleteMapping("/vehicle-types/{id}")
    public ApiResponse<Void> deleteVehicle(@PathVariable Long id) {
        adminService.deleteVehicle(id);
        return ApiResponse.ok();
    }

    @GetMapping("/pricing-rules")
    public ApiResponse<List<Entities.PricingRule>> pricingRules() {
        return ApiResponse.ok(pricingService.pricingRules());
    }

    @PostMapping("/pricing-rules")
    public ApiResponse<Void> saveRule(@Valid @RequestBody AdminDtos.PricingRuleReq req) {
        adminService.saveRule(req);
        return ApiResponse.ok();
    }

    @GetMapping("/reviews")
    public ApiResponse<List<Map<String, Object>>> reviews() {
        return ApiResponse.ok(mapper.listReviews());
    }

    @PostMapping("/reviews/{id}/hidden")
    public ApiResponse<Void> hideReview(@PathVariable Long id, @RequestBody Map<String, Object> req) {
        adminService.hideReview(id, Integer.valueOf(String.valueOf(req.get("hidden"))));
        return ApiResponse.ok();
    }
}
