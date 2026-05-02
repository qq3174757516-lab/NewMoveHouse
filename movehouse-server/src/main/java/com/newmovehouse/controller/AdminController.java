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

/**
 * 管理端：看板、用户/司机、订单、车型与计价、评价、公告、投诉。
 */
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

    /** 管理看板 */
    @GetMapping("/dashboard")
    public ApiResponse<Map<String, Object>> dashboard() {
        return ApiResponse.ok(adminService.dashboard());
    }

    /** 用户列表 */
    @GetMapping("/users")
    public ApiResponse<List<Map<String, Object>>> users() {
        return ApiResponse.ok(adminService.users());
    }

    /** 更新用户状态 */
    @PostMapping("/users/{id}/status")
    public ApiResponse<Void> userStatus(@PathVariable Long id, @RequestBody Map<String, Object> req) {
        adminService.userStatus(id, Integer.valueOf(String.valueOf(req.get("status"))));
        return ApiResponse.ok();
    }

    /** 司机列表 */
    @GetMapping("/drivers")
    public ApiResponse<List<Map<String, Object>>> drivers(@RequestParam(required = false) String auditStatus) {
        return ApiResponse.ok(adminService.drivers(auditStatus));
    }

    /** 司机资质文件 */
    @GetMapping("/drivers/{id}/documents")
    public ApiResponse<List<Entities.DriverDocument>> driverDocuments(@PathVariable Long id) {
        return ApiResponse.ok(adminService.driverDocuments(id));
    }

    /** 审核司机 */
    @PostMapping("/drivers/audit")
    public ApiResponse<Void> auditDriver(@Valid @RequestBody AdminDtos.AuditDriverReq req) {
        adminService.auditDriver(req);
        return ApiResponse.ok();
    }

    /** 审核司机（兼容旧路径） */
    @PutMapping("/driver/{driverId}/audit")
    public ApiResponse<Void> auditDriverCompat(@PathVariable Long driverId, @RequestBody Map<String, String> req) {
        AdminDtos.AuditDriverReq body = new AdminDtos.AuditDriverReq();
        body.driverId = driverId;
        body.status = req.get("status");
        body.reason = req.get("reason");
        adminService.auditDriver(body);
        return ApiResponse.ok();
    }

    /** 订单列表 */
    @GetMapping("/orders")
    public ApiResponse<List<Map<String, Object>>> orders(@RequestParam(required = false) String status) {
        return ApiResponse.ok(mapper.listAdminOrders(status));
    }

    /** 订单详情 */
    @GetMapping("/orders/{id}")
    public ApiResponse<Map<String, Object>> detail(@PathVariable Long id) {
        return ApiResponse.ok(orderService.detail(id));
    }

    /** 管理员取消订单 */
    @PostMapping("/orders/{id}/cancel")
    public ApiResponse<Map<String, Object>> cancel(@PathVariable Long id, @RequestBody(required = false) OrderDtos.CancelReq req) {
        return ApiResponse.ok(orderService.cancel(id, req == null ? "管理员取消" : req.reason, "ADMIN", UserContext.id()));
    }

    /** 车型列表（含禁用） */
    @GetMapping("/vehicle-types")
    public ApiResponse<List<Entities.VehicleType>> vehicleTypes() {
        return ApiResponse.ok(mapper.listAdminVehicleTypes());
    }

    /** 保存车型 */
    @PostMapping("/vehicle-types")
    public ApiResponse<Object> saveVehicle(@Valid @RequestBody AdminDtos.VehicleTypeReq req) {
        return ApiResponse.ok(adminService.saveVehicle(req));
    }

    /** 删除车型 */
    @DeleteMapping("/vehicle-types/{id}")
    public ApiResponse<Void> deleteVehicle(@PathVariable Long id) {
        adminService.deleteVehicle(id);
        return ApiResponse.ok();
    }

    /** 计价规则列表 */
    @GetMapping("/pricing-rules")
    public ApiResponse<List<Entities.PricingRule>> pricingRules() {
        return ApiResponse.ok(pricingService.pricingRules());
    }

    /** 保存计价规则 */
    @PostMapping("/pricing-rules")
    public ApiResponse<Void> saveRule(@Valid @RequestBody AdminDtos.PricingRuleReq req) {
        adminService.saveRule(req);
        return ApiResponse.ok();
    }

    /** 评价列表 */
    @GetMapping("/reviews")
    public ApiResponse<List<Map<String, Object>>> reviews() {
        return ApiResponse.ok(mapper.listReviews());
    }

    /** 隐藏评价 */
    @PostMapping("/reviews/{id}/hidden")
    public ApiResponse<Void> hideReview(@PathVariable Long id, @RequestBody Map<String, Object> req) {
        adminService.hideReview(id, Integer.valueOf(String.valueOf(req.get("hidden"))));
        return ApiResponse.ok();
    }

    /** 公告列表 */
    @GetMapping("/announcements")
    public ApiResponse<List<Entities.Announcement>> announcements() {
        return ApiResponse.ok(adminService.announcements());
    }

    /** 保存公告 */
    @PostMapping("/announcements")
    public ApiResponse<Void> saveAnnouncement(@Valid @RequestBody AdminDtos.AnnouncementReq req) {
        adminService.saveAnnouncement(req);
        return ApiResponse.ok();
    }

    /** 删除公告 */
    @DeleteMapping("/announcements/{id}")
    public ApiResponse<Void> deleteAnnouncement(@PathVariable Long id) {
        adminService.deleteAnnouncement(id);
        return ApiResponse.ok();
    }

    /** 投诉列表 */
    @GetMapping("/complaints")
    public ApiResponse<List<Map<String, Object>>> complaints(@RequestParam(required = false) String status) {
        return ApiResponse.ok(adminService.complaints(status));
    }

    /** 处理投诉 */
    @PutMapping("/complaints/{id}")
    public ApiResponse<Void> updateComplaint(@PathVariable Long id, @Valid @RequestBody AdminDtos.ComplaintUpdateReq req) {
        adminService.updateComplaint(id, req);
        return ApiResponse.ok();
    }
}
