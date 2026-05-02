package com.newmovehouse.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.newmovehouse.common.UserContext;
import com.newmovehouse.dto.AdminDtos;
import com.newmovehouse.mapper.AppMapper;
import com.newmovehouse.websocket.MoveHouseWebSocketHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Service
public class AdminService {
    @Autowired
    private AppMapper mapper;
    @Autowired
    private PricingService pricingService;
    @Autowired
    private StringRedisTemplate redisTemplate;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private MoveHouseWebSocketHandler webSocketHandler;

    public Map<String, Object> dashboard() {
        return mapper.dashboard();
    }

    public List<Map<String, Object>> users() {
        return mapper.listUsers();
    }

    public void userStatus(Long userId, Integer status) {
        mapper.updateUserStatus(userId, status);
        log("USER_STATUS", "USER", userId, "status=" + status);
    }

    public List<Map<String, Object>> drivers(String auditStatus) {
        return mapper.listDrivers(auditStatus);
    }

    public void auditDriver(AdminDtos.AuditDriverReq req) {
        mapper.updateDriverStatus(req.driverId, req.status);
        mapper.insertDriverAudit(req.driverId, UserContext.id(), req.status, req.reason);
        refreshDriverCache(req.driverId);
        webSocketHandler.pushToDriver(req.driverId, "DRIVER_AUDIT_RESULT", mapper.findDriverById(req.driverId));
        log("DRIVER_AUDIT", "DRIVER", req.driverId, req.status + ":" + req.reason);
    }

    public Object saveVehicle(AdminDtos.VehicleTypeReq req) {
        if (req.id == null) {
            mapper.insertVehicleType(req);
        } else {
            mapper.updateVehicleType(req);
        }
        pricingService.evictCache();
        log("VEHICLE_SAVE", "VEHICLE_TYPE", req.id, req.name);
        return req;
    }

    public void deleteVehicle(Long id) {
        mapper.deleteVehicleType(id);
        pricingService.evictCache();
        log("VEHICLE_DELETE", "VEHICLE_TYPE", id, null);
    }

    public void saveRule(AdminDtos.PricingRuleReq req) {
        mapper.upsertPricingRule(req);
        pricingService.evictCache();
        log("PRICING_RULE_SAVE", "PRICING_RULE", null, req.ruleKey);
    }

    public void hideReview(Long id, Integer hidden) {
        mapper.hideReview(id, hidden);
        log("REVIEW_HIDE", "REVIEW", id, "hidden=" + hidden);
    }

    private void log(String action, String targetType, Long targetId, String detail) {
        mapper.insertAdminLog(UserContext.id(), action, targetType, targetId, detail);
    }

    private void refreshDriverCache(Long driverId) {
        String key = "driver:profile:" + driverId;
        try {
            redisTemplate.delete(key);
            Map<String, Object> driver = mapper.findDriverById(driverId);
            if (driver != null) {
                redisTemplate.opsForValue().set(key, objectMapper.writeValueAsString(driver), 30, TimeUnit.MINUTES);
            }
        } catch (Exception ignored) {
        }
    }
}
