package com.newmovehouse.mapper;

import com.newmovehouse.dto.AdminDtos;
import com.newmovehouse.dto.AuthDtos;
import com.newmovehouse.dto.OrderDtos;
import com.newmovehouse.entity.Entities;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * MyBatis 数据访问接口；具体 SQL 见 {@code classpath:mapper/AppMapper.xml}。
 */
@Mapper
public interface AppMapper {
    /** 按用户名查询普通用户 */
    Map<String, Object> findUserByUsername(@Param("username") String username);
    /** 按用户名查询司机 */
    Map<String, Object> findDriverByUsername(@Param("username") String username);
    /** 按用户名查询管理员 */
    Map<String, Object> findAdminByUsername(@Param("username") String username);
    /** 按 id 查询用户 */
    Map<String, Object> findUserById(@Param("id") Long id);
    /** 按 id 查询司机 */
    Map<String, Object> findDriverById(@Param("id") Long id);
    /** 插入用户注册记录 */
    int insertUser(AuthDtos.RegisterUserReq req, @Param("passwordHash") String passwordHash);
    /** 插入司机注册记录 */
    int insertDriver(AuthDtos.RegisterDriverReq req, @Param("passwordHash") String passwordHash);
    /** 更新用户启用状态 */
    int updateUserStatus(@Param("id") Long id, @Param("status") Integer status);
    /** 更新司机账号/审核状态 */
    int updateDriverStatus(@Param("id") Long id, @Param("status") String status);
    /** 写入司机审核流水 */
    int insertDriverAudit(@Param("driverId") Long driverId, @Param("adminId") Long adminId, @Param("status") String status, @Param("reason") String reason);

    /** 用户常用地址列表 */
    List<Map<String, Object>> listAddresses(@Param("userId") Long userId);
    /** 新增常用地址 */
    int insertAddress(@Param("userId") Long userId, @Param("req") AuthDtos.AddressReq req);
    /** 更新常用地址 */
    int updateAddress(@Param("userId") Long userId, @Param("req") AuthDtos.AddressReq req);
    /** 删除常用地址 */
    int deleteAddress(@Param("userId") Long userId, @Param("id") Long id);

    /** 前台启用车型列表 */
    List<Entities.VehicleType> listVehicleTypes();
    /** 管理端车型列表（含禁用） */
    List<Entities.VehicleType> listAdminVehicleTypes();
    /** 按 id 查车型 */
    Entities.VehicleType getVehicleType(@Param("id") Long id);
    /** 新增车型 */
    int insertVehicleType(AdminDtos.VehicleTypeReq req);
    /** 更新车型 */
    int updateVehicleType(AdminDtos.VehicleTypeReq req);
    /** 删除车型 */
    int deleteVehicleType(@Param("id") Long id);

    /** 计价规则列表 */
    List<Entities.PricingRule> listPricingRules();
    /** 按 key 查规则 */
    Entities.PricingRule getPricingRule(@Param("ruleKey") String ruleKey);
    /** 插入或更新计价规则 */
    int upsertPricingRule(AdminDtos.PricingRuleReq req);

    /** 插入订单主表 */
    int insertOrder(@Param("req") OrderDtos.CreateOrderReq req,
                    @Param("userId") Long userId,
                    @Param("distanceKm") BigDecimal distanceKm,
                    @Param("amount") BigDecimal amount);
    /** 订单详情 */
    Map<String, Object> getOrder(@Param("id") Long id);
    /** 用户订单列表 */
    List<Map<String, Object>> listUserOrders(@Param("userId") Long userId, @Param("status") String status);
    /** 司机订单列表 */
    List<Map<String, Object>> listDriverOrders(@Param("driverId") Long driverId, @Param("status") String status);
    /** 接单大厅待抢单 */
    List<Map<String, Object>> listHallOrders();
    /** 管理端订单列表 */
    List<Map<String, Object>> listAdminOrders(@Param("status") String status);
    /** 司机抢单 CAS 更新 */
    int acceptOrder(@Param("orderId") Long orderId, @Param("driverId") Long driverId);
    /** 订单状态流转 */
    int updateOrderStatus(@Param("orderId") Long orderId, @Param("from") String from, @Param("to") String to);
    /** 用户/司机侧取消（状态受限） */
    int cancelOrder(@Param("orderId") Long orderId, @Param("reason") String reason);
    /** 管理员强制取消 */
    int cancelOrderAdmin(@Param("orderId") Long orderId, @Param("reason") String reason);
    /** 支付完成更新订单 */
    int payOrder(@Param("orderId") Long orderId);
    /** 状态变更日志 */
    int insertStatusLog(@Param("orderId") Long orderId, @Param("operatorRole") String operatorRole,
                        @Param("operatorId") Long operatorId, @Param("fromStatus") String fromStatus,
                        @Param("toStatus") String toStatus, @Param("remark") String remark);
    /** 订单状态日志列表 */
    List<Map<String, Object>> listOrderLogs(@Param("orderId") Long orderId);

    /** 支付流水 */
    int insertPayment(@Param("orderId") Long orderId, @Param("userId") Long userId, @Param("amount") BigDecimal amount, @Param("paymentMethod") String paymentMethod);
    /** 按订单查评价 */
    Map<String, Object> getReviewByOrder(@Param("orderId") Long orderId);
    /** 插入评价 */
    int insertReview(@Param("req") OrderDtos.ReviewReq req, @Param("userId") Long userId, @Param("driverId") Long driverId);
    /** 管理端评价列表 */
    List<Map<String, Object>> listReviews();
    /** 隐藏/显示评价 */
    int hideReview(@Param("id") Long id, @Param("hidden") Integer hidden);

    /** 司机部分资料更新 */
    int updateDriverProfile(@Param("driverId") Long driverId, @Param("vehicleTypeId") Long vehicleTypeId,
                            @Param("vehiclePlate") String vehiclePlate, @Param("phone") String phone);
    /** 司机收入汇总 */
    Map<String, Object> driverIncome(@Param("driverId") Long driverId);

    /** 司机收入看板汇总 */
    Map<String, Object> driverIncomeDashboard(@Param("driverId") Long driverId);

    /** 司机近 7 日收入趋势 */
    List<Map<String, Object>> driverIncomeTrend7d(@Param("driverId") Long driverId);

    /** 管理端公告列表 */
    List<Entities.Announcement> listAnnouncementsAdmin();

    /** 新增公告 */
    int insertAnnouncement(AdminDtos.AnnouncementReq req);

    /** 更新公告 */
    int updateAnnouncement(AdminDtos.AnnouncementReq req);

    /** 删除公告 */
    int deleteAnnouncement(@Param("id") Long id);

    /** 按受众查询启用公告 */
    List<Entities.Announcement> listAnnouncementsByAudience(@Param("audience") String audience);

    /** 司机资质文件插入 */
    int insertDriverDocument(@Param("driverId") Long driverId, @Param("docType") String docType,
                             @Param("fileUrl") String fileUrl, @Param("originalName") String originalName);

    /** 司机端资质列表 */
    List<Entities.DriverDocument> listDriverDocuments(@Param("driverId") Long driverId);

    /** 管理端查看司机资质 */
    List<Entities.DriverDocument> listDriverDocumentsAdmin(@Param("driverId") Long driverId);

    /** 更新用户资料 */
    int updateUserProfile(@Param("userId") Long userId, @Param("phone") String phone, @Param("nickname") String nickname);

    /** 司机全量资料更新（可触发重新审核） */
    int updateDriverProfileFull(@Param("driverId") Long driverId,
                                @Param("realName") String realName,
                                @Param("phone") String phone,
                                @Param("vehicleTypeId") Long vehicleTypeId,
                                @Param("vehiclePlate") String vehiclePlate);

    /** 仅更新公告启用状态 */
    int updateAnnouncementEnabled(@Param("id") Long id, @Param("enabled") Integer enabled);

    /** 插入投诉 */
    int insertComplaint(@Param("orderId") Long orderId,
                        @Param("userId") Long userId,
                        @Param("driverId") Long driverId,
                        @Param("title") String title,
                        @Param("content") String content,
                        @Param("imageUrl") String imageUrl);

    /** 按订单查投诉 */
    Map<String, Object> getComplaintByOrder(@Param("orderId") Long orderId);

    /** 用户投诉列表 */
    List<Map<String, Object>> listUserComplaints(@Param("userId") Long userId);

    /** 司机被投诉列表 */
    List<Map<String, Object>> listDriverComplaints(@Param("driverId") Long driverId);

    /** 管理端投诉列表 */
    List<Map<String, Object>> listAdminComplaints(@Param("status") String status);

    /** 更新投诉处理结果 */
    int updateComplaint(@Param("id") Long id,
                        @Param("status") String status,
                        @Param("adminRemark") String adminRemark,
                        @Param("actionType") String actionType,
                        @Param("penaltyAmount") java.math.BigDecimal penaltyAmount);

    /** 司机未结案投诉数量 */
    int countDriverOpenComplaints(@Param("driverId") Long driverId);

    /** 管理端用户列表 */
    List<Map<String, Object>> listUsers();
    /** 管理端司机列表 */
    List<Map<String, Object>> listDrivers(@Param("auditStatus") String auditStatus);
    /** 管理端看板汇总指标 */
    Map<String, Object> dashboard();

    /** 管理端近 7 日订单量趋势 */
    List<Map<String, Object>> adminOrdersTrend7d();
    /** 管理员操作日志 */
    int insertAdminLog(@Param("adminId") Long adminId, @Param("action") String action, @Param("targetType") String targetType,
                       @Param("targetId") Long targetId, @Param("detail") String detail);
}
