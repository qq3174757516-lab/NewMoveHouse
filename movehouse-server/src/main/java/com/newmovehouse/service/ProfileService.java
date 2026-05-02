package com.newmovehouse.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.newmovehouse.common.BizException;
import com.newmovehouse.common.UserContext;
import com.newmovehouse.dto.AuthDtos;
import com.newmovehouse.entity.Entities;
import com.newmovehouse.mapper.AppMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Service
public class ProfileService {
    @Autowired
    private AppMapper mapper;
    @Autowired
    private StringRedisTemplate redisTemplate;
    @Autowired
    private ObjectMapper objectMapper;

    public Object me() {
        if ("USER".equals(UserContext.role())) {
            return mapper.findUserById(UserContext.id());
        }
        if ("DRIVER".equals(UserContext.role())) {
            Map<String, Object> driver = mapper.findDriverById(UserContext.id());
            try {
                redisTemplate.opsForValue().set("driver:profile:" + UserContext.id(), objectMapper.writeValueAsString(driver), 30, TimeUnit.MINUTES);
            } catch (Exception ignored) {
            }
            return driver;
        }
        return null;
    }

    public List<Map<String, Object>> addresses() {
        return mapper.listAddresses(UserContext.id());
    }

    public void saveAddress(AuthDtos.AddressReq req) {
        if (req.id == null) {
            mapper.insertAddress(UserContext.id(), req);
        } else {
            mapper.updateAddress(UserContext.id(), req);
        }
    }

    public void deleteAddress(Long id) {
        mapper.deleteAddress(UserContext.id(), id);
    }

    public void updateDriverProfile(Long vehicleTypeId, String vehiclePlate, String phone, String serviceArea) {
        mapper.updateDriverProfile(UserContext.id(), vehicleTypeId, vehiclePlate, phone, serviceArea);
    }

    public void reportLocation(BigDecimal lng, BigDecimal lat) {
        Entities.DriverLocation loc = new Entities.DriverLocation();
        loc.driverId = UserContext.id();
        loc.lng = lng;
        loc.lat = lat;
        loc.reportTime = LocalDateTime.now();
        try {
            redisTemplate.opsForValue().set("driver:location:" + loc.driverId, objectMapper.writeValueAsString(loc), 5, TimeUnit.MINUTES);
        } catch (Exception e) {
            throw new BizException("位置上报失败");
        }
    }

    public Object driverLocation(Long driverId) {
        String val = redisTemplate.opsForValue().get("driver:location:" + driverId);
        if (val == null) {
            return null;
        }
        try {
            return objectMapper.readValue(val, Map.class);
        } catch (Exception e) {
            return null;
        }
    }

    public Map<String, Object> driverIncome() {
        return mapper.driverIncome(UserContext.id());
    }
}
