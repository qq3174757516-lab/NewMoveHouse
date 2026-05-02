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

@RestController
@RequestMapping("/api/user")
public class UserController {
    @Autowired
    private ProfileService profileService;
    @Autowired
    private OrderService orderService;

    @GetMapping("/me")
    public ApiResponse<Object> me() {
        return ApiResponse.ok(profileService.me());
    }

    @GetMapping("/addresses")
    public ApiResponse<List<Map<String, Object>>> addresses() {
        return ApiResponse.ok(profileService.addresses());
    }

    @PostMapping("/addresses")
    public ApiResponse<Void> saveAddress(@Valid @RequestBody AuthDtos.AddressReq req) {
        profileService.saveAddress(req);
        return ApiResponse.ok();
    }

    @DeleteMapping("/addresses/{id}")
    public ApiResponse<Void> deleteAddress(@PathVariable Long id) {
        profileService.deleteAddress(id);
        return ApiResponse.ok();
    }

    @PostMapping("/orders")
    public ApiResponse<Map<String, Object>> create(@Valid @RequestBody OrderDtos.CreateOrderReq req) {
        return ApiResponse.ok(orderService.create(req));
    }

    @GetMapping("/orders")
    public ApiResponse<List<Map<String, Object>>> orders(@RequestParam(required = false) String status) {
        return ApiResponse.ok(orderService.userOrders(status));
    }

    @GetMapping("/orders/{id}")
    public ApiResponse<Map<String, Object>> detail(@PathVariable Long id) {
        return ApiResponse.ok(orderService.detail(id));
    }

    @PostMapping("/orders/{id}/pay")
    public ApiResponse<Map<String, Object>> pay(@PathVariable Long id, @RequestBody(required = false) Map<String, String> req) {
        return ApiResponse.ok(orderService.pay(id, req == null ? null : req.get("paymentMethod")));
    }

    @PostMapping("/order/{id}/pay")
    public ApiResponse<Map<String, Object>> payCompat(@PathVariable Long id, @RequestBody(required = false) Map<String, String> req) {
        return ApiResponse.ok(orderService.pay(id, req == null ? null : req.get("paymentMethod")));
    }

    @PostMapping("/orders/{id}/cancel")
    public ApiResponse<Map<String, Object>> cancel(@PathVariable Long id, @RequestBody(required = false) OrderDtos.CancelReq req) {
        return ApiResponse.ok(orderService.cancel(id, req == null ? "用户取消" : req.reason, "USER", UserContext.id()));
    }

    @PostMapping("/reviews")
    public ApiResponse<Void> review(@Valid @RequestBody OrderDtos.ReviewReq req) {
        orderService.review(req);
        return ApiResponse.ok();
    }
}
