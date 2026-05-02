package com.newmovehouse.controller;

import com.newmovehouse.common.ApiResponse;
import com.newmovehouse.service.OrderService;
import com.newmovehouse.service.ProfileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/driver")
public class DriverController {
    @Autowired
    private ProfileService profileService;
    @Autowired
    private OrderService orderService;

    @GetMapping("/me")
    public ApiResponse<Object> me() {
        return ApiResponse.ok(profileService.me());
    }

    @PostMapping("/profile")
    public ApiResponse<Void> profile(@RequestBody Map<String, Object> req) {
        profileService.updateDriverProfile(
                Long.valueOf(String.valueOf(req.get("vehicleTypeId"))),
                String.valueOf(req.get("vehiclePlate")),
                String.valueOf(req.get("phone")),
                String.valueOf(req.get("serviceArea"))
        );
        return ApiResponse.ok();
    }

    @GetMapping("/hall")
    public ApiResponse<List<Map<String, Object>>> hall() {
        return ApiResponse.ok(orderService.hall());
    }

    @PostMapping("/orders/{id}/accept")
    public ApiResponse<Map<String, Object>> accept(@PathVariable Long id) {
        return ApiResponse.ok(orderService.accept(id));
    }

    @GetMapping("/orders")
    public ApiResponse<List<Map<String, Object>>> orders(@RequestParam(required = false) String status) {
        return ApiResponse.ok(orderService.driverOrders(status));
    }

    @GetMapping("/orders/{id}")
    public ApiResponse<Map<String, Object>> detail(@PathVariable Long id) {
        return ApiResponse.ok(orderService.detail(id));
    }

    @PostMapping("/orders/{id}/status")
    public ApiResponse<Map<String, Object>> status(@PathVariable Long id, @RequestBody Map<String, String> req) {
        return ApiResponse.ok(orderService.nextStatus(id, req.get("action")));
    }

    @PostMapping("/location")
    public ApiResponse<Void> location(@RequestBody Map<String, Object> req) {
        profileService.reportLocation(new BigDecimal(String.valueOf(req.get("lng"))), new BigDecimal(String.valueOf(req.get("lat"))));
        return ApiResponse.ok();
    }

    @GetMapping("/income")
    public ApiResponse<Map<String, Object>> income() {
        return ApiResponse.ok(profileService.driverIncome());
    }
}

