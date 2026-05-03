package com.newmovehouse.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.newmovehouse.common.UserContext;
import com.newmovehouse.dto.AdminDtos;
import com.newmovehouse.entity.Entities;
import com.newmovehouse.mapper.AppMapper;
import com.newmovehouse.websocket.MoveHouseWebSocketHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * 管理端业务：看板、用户/司机/订单、车型与计价、公告、投诉处理、评价隐藏等。
 */
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

    /** 管理端首页统计，附带近 7 日订单趋势 */
    public Map<String, Object> dashboard() {
        Map<String, Object> m = mapper.dashboard();
        if (m == null) {
            m = new java.util.HashMap<>();
        } else {
            m = new java.util.HashMap<>(m);
        }
        m.put("ordersTrend7d", mapper.adminOrdersTrend7d());
        return m;
    }

    /** 用户列表 */
    public List<Map<String, Object>> users() {
        return mapper.listUsers();
    }

    /** 启用/禁用用户 */
    public void userStatus(Long userId, Integer status) {
        mapper.updateUserStatus(userId, status);
        log("USER_STATUS", "USER", userId, "status=" + status);
    }

    /** 司机列表，可按审核状态筛选 */
    public List<Map<String, Object>> drivers(String auditStatus) {
        return mapper.listDrivers(auditStatus);
    }

    /** 审核司机并推送 WebSocket、刷新缓存 */
    public void auditDriver(AdminDtos.AuditDriverReq req) {
        mapper.updateDriverStatus(req.driverId, req.status);
        mapper.insertDriverAudit(req.driverId, UserContext.id(), req.status, req.reason);
        refreshDriverCache(req.driverId);
        webSocketHandler.pushToDriver(req.driverId, "DRIVER_AUDIT_RESULT", mapper.findDriverById(req.driverId));
        log("DRIVER_AUDIT", "DRIVER", req.driverId, req.status + ":" + req.reason);
    }

    /** 新增或更新车型，并清理计价缓存 */
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

    /** 删除车型 */
    public void deleteVehicle(Long id) {
        mapper.deleteVehicleType(id);
        pricingService.evictCache();
        log("VEHICLE_DELETE", "VEHICLE_TYPE", id, null);
    }

    /** 保存计价规则 */
    public void saveRule(AdminDtos.PricingRuleReq req) {
        mapper.upsertPricingRule(req);
        pricingService.evictCache();
        log("PRICING_RULE_SAVE", "PRICING_RULE", null, req.ruleKey);
    }

    /** 隐藏或展示评价 */
    public void hideReview(Long id, Integer hidden) {
        mapper.hideReview(id, hidden);
        log("REVIEW_HIDE", "REVIEW", id, "hidden=" + hidden);
    }

    /** 全部公告（管理端） */
    public List<Entities.Announcement> announcements() {
        return mapper.listAnnouncementsAdmin();
    }

    /** 新增或更新公告 */
    public void saveAnnouncement(AdminDtos.AnnouncementReq req) {
        if (req.id == null) {
            mapper.insertAnnouncement(req);
            log("ANNOUNCEMENT_CREATE", "ANNOUNCEMENT", null, req.title);
        } else {
            mapper.updateAnnouncement(req);
            log("ANNOUNCEMENT_UPDATE", "ANNOUNCEMENT", req.id, req.title);
        }
    }

    /** 删除公告 */
    public void deleteAnnouncement(Long id) {
        mapper.deleteAnnouncement(id);
        log("ANNOUNCEMENT_DELETE", "ANNOUNCEMENT", id, null);
    }

    /** 仅切换公告启用状态（无需打开编辑弹窗） */
    public void setAnnouncementEnabled(Long id, int enabled) {
        mapper.updateAnnouncementEnabled(id, enabled);
        log("ANNOUNCEMENT_ENABLE", "ANNOUNCEMENT", id, enabled == 1 ? "启用" : "禁用");
    }

    /** 某司机资质文件列表（管理端查看） */
    public List<Entities.DriverDocument> driverDocuments(Long driverId) {
        return mapper.listDriverDocumentsAdmin(driverId);
    }

    /** 投诉工单列表 */
    public List<Map<String, Object>> complaints(String status) {
        return mapper.listAdminComplaints(status);
    }

    /** 处理投诉：状态、备注、扣薪等 */
    public void updateComplaint(Long id, AdminDtos.ComplaintUpdateReq req) {
        java.math.BigDecimal penalty = req.penaltyAmount == null ? java.math.BigDecimal.ZERO : req.penaltyAmount;
        String actionType = req.actionType == null ? "NONE" : req.actionType;
        if (!"DEDUCT".equals(actionType)) {
            penalty = java.math.BigDecimal.ZERO;
        }
        mapper.updateComplaint(id, req.status, req.adminRemark, actionType, penalty);
        log("COMPLAINT_UPDATE", "COMPLAINT", id, req.status);
    }

    /** 写入管理员操作日志 */
    private void log(String action, String targetType, Long targetId, String detail) {
        mapper.insertAdminLog(UserContext.id(), action, targetType, targetId, detail);
    }

    /** 删除并重建司机资料 Redis 缓存 */
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
