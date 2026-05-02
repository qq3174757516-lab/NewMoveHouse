package com.newmovehouse.service;

import com.newmovehouse.common.BizException;
import com.newmovehouse.dto.OrderDtos;
import com.newmovehouse.entity.Entities;
import com.newmovehouse.mapper.AppMapper;
import com.newmovehouse.util.AmapClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Service
public class PricingService {
    @Autowired
    private AppMapper mapper;
    @Autowired
    private AmapClient amapClient;
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    public List<Entities.VehicleType> vehicleTypes() {
        String key = "cache:vehicle-types";
        Object cached = redisTemplate.opsForValue().get(key);
        if (cached instanceof List) {
            return (List<Entities.VehicleType>) cached;
        }
        List<Entities.VehicleType> list = mapper.listVehicleTypes();
        redisTemplate.opsForValue().set(key, list, 10, TimeUnit.MINUTES);
        return list;
    }

    public List<Entities.PricingRule> pricingRules() {
        String key = "cache:pricing-rules";
        Object cached = redisTemplate.opsForValue().get(key);
        if (cached instanceof List) {
            return (List<Entities.PricingRule>) cached;
        }
        List<Entities.PricingRule> list = mapper.listPricingRules();
        redisTemplate.opsForValue().set(key, list, 10, TimeUnit.MINUTES);
        return list;
    }

    public Map<String, Object> estimate(OrderDtos.EstimateReq req) {
        Entities.VehicleType vt = mapper.getVehicleType(req.vehicleTypeId);
        if (vt == null || vt.enabled == null || vt.enabled == 0) {
            throw new BizException("车型不可用");
        }
        BigDecimal distanceKm = amapClient.drivingDistanceKm(req.startLng, req.startLat, req.endLng, req.endLat);
        BigDecimal mileageFee = vt.perKmPrice.multiply(distanceKm);
        BigDecimal floorFee = floorFee(req.startHasElevator, req.startFloor).add(floorFee(req.endHasElevator, req.endFloor));
        BigDecimal largeItemFee = rule("large_item_fee", "30").multiply(BigDecimal.valueOf(nvl(req.largeItemCount)));
        BigDecimal nightFee = isNight(req) ? rule("night_service_fee", "50") : BigDecimal.ZERO;
        BigDecimal total = vt.basePrice.add(mileageFee).add(floorFee).add(largeItemFee).add(nightFee).setScale(2, RoundingMode.HALF_UP);
        Map<String, Object> map = new HashMap<>();
        map.put("vehicleBasePrice", vt.basePrice);
        map.put("distanceKm", distanceKm);
        map.put("mileageFee", mileageFee.setScale(2, RoundingMode.HALF_UP));
        map.put("floorFee", floorFee);
        map.put("largeItemFee", largeItemFee);
        map.put("nightFee", nightFee);
        map.put("estimatedAmount", total);
        return map;
    }

    public BigDecimal estimateAmount(OrderDtos.EstimateReq req) {
        return (BigDecimal) estimate(req).get("estimatedAmount");
    }

    public BigDecimal distanceKm(OrderDtos.EstimateReq req) {
        return amapClient.drivingDistanceKm(req.startLng, req.startLat, req.endLng, req.endLat);
    }

    public void evictCache() {
        redisTemplate.delete("cache:vehicle-types");
        redisTemplate.delete("cache:pricing-rules");
    }

    private BigDecimal floorFee(Boolean hasElevator, Integer floor) {
        if (Boolean.TRUE.equals(hasElevator)) {
            return BigDecimal.ZERO;
        }
        int extraFloor = Math.max(0, nvl(floor) - 1);
        return rule("floor_fee_per_floor", "10").multiply(BigDecimal.valueOf(extraFloor));
    }

    private boolean isNight(OrderDtos.EstimateReq req) {
        if (req.appointmentTime == null) {
            return false;
        }
        LocalTime time = req.appointmentTime.toLocalTime();
        return time.isAfter(LocalTime.of(22, 0)) || time.isBefore(LocalTime.of(6, 0));
    }

    private BigDecimal rule(String key, String def) {
        Entities.PricingRule rule = mapper.getPricingRule(key);
        return rule == null ? new BigDecimal(def) : rule.ruleValue;
    }

    private int nvl(Integer v) {
        return v == null ? 0 : v;
    }
}

