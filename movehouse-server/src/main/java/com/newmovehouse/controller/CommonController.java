package com.newmovehouse.controller;

import com.newmovehouse.common.ApiResponse;
import com.newmovehouse.dto.OrderDtos;
import com.newmovehouse.entity.Entities;
import com.newmovehouse.service.PricingService;
import com.newmovehouse.service.ProfileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/common")
public class CommonController {
    @Autowired
    private PricingService pricingService;
    @Autowired
    private ProfileService profileService;

    @GetMapping("/vehicle-types")
    public ApiResponse<List<Entities.VehicleType>> vehicleTypes() {
        return ApiResponse.ok(pricingService.vehicleTypes());
    }

    @PostMapping("/estimate")
    public ApiResponse<Map<String, Object>> estimate(@Valid @RequestBody OrderDtos.EstimateReq req) {
        return ApiResponse.ok(pricingService.estimate(req));
    }

    @GetMapping("/driver-location/{driverId}")
    public ApiResponse<Object> driverLocation(@PathVariable Long driverId) {
        return ApiResponse.ok(profileService.driverLocation(driverId));
    }
}

