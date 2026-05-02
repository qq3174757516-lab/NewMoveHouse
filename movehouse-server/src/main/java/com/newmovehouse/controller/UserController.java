package com.newmovehouse.controller;

import com.newmovehouse.common.ApiResponse;
import com.newmovehouse.common.UserContext;
import com.newmovehouse.dto.AuthDtos;
import com.newmovehouse.dto.OrderDtos;
import com.newmovehouse.service.OrderService;
import com.newmovehouse.service.ProfileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;
import java.util.Map;

/**
 * 用户端：个人资料、地址、下单、支付、取消、评价、投诉。
 */
@RestController
@RequestMapping("/api/user")
public class UserController {
    @Autowired
    private ProfileService profileService;
    @Autowired
    private OrderService orderService;

    /** 当前用户资料 */
    @GetMapping("/me")
    public ApiResponse<Object> me() {
        return ApiResponse.ok(profileService.me());
    }

    /** 更新昵称、电话 */
    @PutMapping("/profile")
    public ApiResponse<Void> updateProfile(@Valid @RequestBody AuthDtos.UpdateUserProfileReq req) {
        profileService.updateUserProfile(req);
        return ApiResponse.ok();
    }

    /** 常用地址列表 */
    @GetMapping("/addresses")
    public ApiResponse<List<Map<String, Object>>> addresses() {
        return ApiResponse.ok(profileService.addresses());
    }

    /** 新增或保存常用地址 */
    @PostMapping("/addresses")
    public ApiResponse<Void> saveAddress(@Valid @RequestBody AuthDtos.AddressReq req) {
        profileService.saveAddress(req);
        return ApiResponse.ok();
    }

    /** 删除常用地址 */
    @DeleteMapping("/addresses/{id}")
    public ApiResponse<Void> deleteAddress(@PathVariable Long id) {
        profileService.deleteAddress(id);
        return ApiResponse.ok();
    }

    /** 创建订单 */
    @PostMapping("/orders")
    public ApiResponse<Map<String, Object>> create(@Valid @RequestBody OrderDtos.CreateOrderReq req) {
        return ApiResponse.ok(orderService.create(req));
    }

    /** 我的订单列表 */
    @GetMapping("/orders")
    public ApiResponse<List<Map<String, Object>>> orders(@RequestParam(required = false) String status) {
        return ApiResponse.ok(orderService.userOrders(status));
    }

    /** 订单详情 */
    @GetMapping("/orders/{id}")
    public ApiResponse<Map<String, Object>> detail(@PathVariable Long id) {
        return ApiResponse.ok(orderService.detail(id));
    }

    /** 模拟支付 */
    @PostMapping("/orders/{id}/pay")
    public ApiResponse<Map<String, Object>> pay(@PathVariable Long id, @RequestBody(required = false) Map<String, String> req) {
        return ApiResponse.ok(orderService.pay(id, req == null ? null : req.get("paymentMethod")));
    }

    /** 模拟支付（兼容旧路径） */
    @PostMapping("/order/{id}/pay")
    public ApiResponse<Map<String, Object>> payCompat(@PathVariable Long id, @RequestBody(required = false) Map<String, String> req) {
        return ApiResponse.ok(orderService.pay(id, req == null ? null : req.get("paymentMethod")));
    }

    /** 用户取消订单 */
    @PostMapping("/orders/{id}/cancel")
    public ApiResponse<Map<String, Object>> cancel(@PathVariable Long id, @RequestBody(required = false) OrderDtos.CancelReq req) {
        return ApiResponse.ok(orderService.cancel(id, req == null ? "用户取消" : req.reason, "USER", UserContext.id()));
    }

    /** 提交评价 */
    @PostMapping("/reviews")
    public ApiResponse<Void> review(@Valid @RequestBody OrderDtos.ReviewReq req) {
        orderService.review(req);
        return ApiResponse.ok();
    }

    /** 提交投诉 */
    @PostMapping("/complaints")
    public ApiResponse<Void> complaint(@Valid @RequestBody OrderDtos.ComplaintReq req) {
        orderService.complaint(req);
        return ApiResponse.ok();
    }

    /** 我的投诉列表 */
    @GetMapping("/complaints")
    public ApiResponse<List<Map<String, Object>>> complaints() {
        return ApiResponse.ok(orderService.userComplaints());
    }
}
