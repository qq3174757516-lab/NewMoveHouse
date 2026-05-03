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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * 用户/司机资料、常用地址、位置上报与司机收入统计。
 */
@Service
public class ProfileService {
    @Autowired
    private AppMapper mapper;
    @Autowired
    private StringRedisTemplate redisTemplate;
    @Autowired
    private ObjectMapper objectMapper;

    /** 当前登录者资料（用户或司机） */
    public Object me() {
        if ("USER".equals(UserContext.role())) {
            return mapper.findUserById(UserContext.id());
        }
        if ("DRIVER".equals(UserContext.role())) {
            Map<String, Object> driver = mapper.findDriverById(UserContext.id());
            cacheDriverProfile(UserContext.id(), driver);
            return driver;
        }
        return null;
    }

    /** 更新用户昵称、电话 */
    public void updateUserProfile(AuthDtos.UpdateUserProfileReq req) {
        mapper.updateUserProfile(UserContext.id(), req.phone, req.nickname);
    }

    /** 用户常用地址列表 */
    public List<Map<String, Object>> addresses() {
        return mapper.listAddresses(UserContext.id());
    }

    /** 新增或修改常用地址 */
    public void saveAddress(AuthDtos.AddressReq req) {
        if (req.id == null) {
            mapper.insertAddress(UserContext.id(), req);
        } else {
            mapper.updateAddress(UserContext.id(), req);
        }
    }

    /** 删除常用地址 */
    public void deleteAddress(Long id) {
        mapper.deleteAddress(UserContext.id(), id);
    }

    /** 司机全量资料更新，修改后通常需重新审核 */
    public void updateDriverProfile(AuthDtos.UpdateDriverProfileReq req) {
        mapper.updateDriverProfileFull(UserContext.id(), req.realName, req.phone, req.vehicleTypeId, req.vehiclePlate);
        Map<String, Object> driver = mapper.findDriverById(UserContext.id());
        cacheDriverProfile(UserContext.id(), driver);
    }

    /** 司机上报实时位置（短期存 Redis） */
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

    /** 读取某司机最近一次上报位置（Map 或 null） */
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

    /** 司机收入汇总（单笔口径由 SQL 定义） */
    public Map<String, Object> driverIncome() {
        return mapper.driverIncome(UserContext.id());
    }

    /** 司机收入看板：汇总 + 近 7 日趋势 */
    public Map<String, Object> driverIncomeDashboard() {
        Map<String, Object> summary = mapper.driverIncomeDashboard(UserContext.id());
        List<Map<String, Object>> trend = mapper.driverIncomeTrend7d(UserContext.id());
        Map<String, Object> res = new HashMap<>();
        if (summary != null) {
            res.putAll(summary);
        }
        res.put("trend7d", trend);
        return res;
    }

    /** 当前司机已上传资质列表 */
    public List<Entities.DriverDocument> driverDocuments() {
        return mapper.listDriverDocuments(UserContext.id());
    }

    /** 记录一条资质文件 */
    public void addDriverDocument(String docType, String fileUrl, String originalName) {
        mapper.insertDriverDocument(UserContext.id(), docType, fileUrl, originalName);
    }

    /** 管理端按司机 id 查看资质（复用 mapper 管理端方法） */
    public List<Entities.DriverDocument> driverDocumentsAdmin(Long driverId) {
        return mapper.listDriverDocumentsAdmin(driverId);
    }

    /** 写入司机资料 Redis 缓存，供拦截器刷新审核状态 */
    private void cacheDriverProfile(Long driverId, Map<String, Object> driver) {
        if (driver == null) {
            return;
        }
        try {
            redisTemplate.opsForValue().set("driver:profile:" + driverId, objectMapper.writeValueAsString(driver), 30, TimeUnit.MINUTES);
        } catch (Exception ignored) {
        }
    }
}
