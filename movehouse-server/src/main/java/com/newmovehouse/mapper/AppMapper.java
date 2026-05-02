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

@Mapper
public interface AppMapper {
    Map<String, Object> findUserByUsername(@Param("username") String username);
    Map<String, Object> findDriverByUsername(@Param("username") String username);
    Map<String, Object> findAdminByUsername(@Param("username") String username);
    Map<String, Object> findUserById(@Param("id") Long id);
    Map<String, Object> findDriverById(@Param("id") Long id);
    int insertUser(AuthDtos.RegisterUserReq req, @Param("passwordHash") String passwordHash);
    int insertDriver(AuthDtos.RegisterDriverReq req, @Param("passwordHash") String passwordHash);
    int updateUserStatus(@Param("id") Long id, @Param("status") Integer status);
    int updateDriverStatus(@Param("id") Long id, @Param("status") String status);
    int insertDriverAudit(@Param("driverId") Long driverId, @Param("adminId") Long adminId, @Param("status") String status, @Param("reason") String reason);

    List<Map<String, Object>> listAddresses(@Param("userId") Long userId);
    int insertAddress(@Param("userId") Long userId, @Param("req") AuthDtos.AddressReq req);
    int updateAddress(@Param("userId") Long userId, @Param("req") AuthDtos.AddressReq req);
    int deleteAddress(@Param("userId") Long userId, @Param("id") Long id);

    List<Entities.VehicleType> listVehicleTypes();
    List<Entities.VehicleType> listAdminVehicleTypes();
    Entities.VehicleType getVehicleType(@Param("id") Long id);
    int insertVehicleType(AdminDtos.VehicleTypeReq req);
    int updateVehicleType(AdminDtos.VehicleTypeReq req);
    int deleteVehicleType(@Param("id") Long id);

    List<Entities.PricingRule> listPricingRules();
    Entities.PricingRule getPricingRule(@Param("ruleKey") String ruleKey);
    int upsertPricingRule(AdminDtos.PricingRuleReq req);

    int insertOrder(@Param("req") OrderDtos.CreateOrderReq req,
                    @Param("userId") Long userId,
                    @Param("distanceKm") BigDecimal distanceKm,
                    @Param("amount") BigDecimal amount);
    Map<String, Object> getOrder(@Param("id") Long id);
    List<Map<String, Object>> listUserOrders(@Param("userId") Long userId, @Param("status") String status);
    List<Map<String, Object>> listDriverOrders(@Param("driverId") Long driverId, @Param("status") String status);
    List<Map<String, Object>> listHallOrders();
    List<Map<String, Object>> listAdminOrders(@Param("status") String status);
    int acceptOrder(@Param("orderId") Long orderId, @Param("driverId") Long driverId);
    int updateOrderStatus(@Param("orderId") Long orderId, @Param("from") String from, @Param("to") String to);
    int cancelOrder(@Param("orderId") Long orderId, @Param("reason") String reason);
    int payOrder(@Param("orderId") Long orderId);
    int insertStatusLog(@Param("orderId") Long orderId, @Param("operatorRole") String operatorRole,
                        @Param("operatorId") Long operatorId, @Param("fromStatus") String fromStatus,
                        @Param("toStatus") String toStatus, @Param("remark") String remark);
    List<Map<String, Object>> listOrderLogs(@Param("orderId") Long orderId);

    int insertPayment(@Param("orderId") Long orderId, @Param("userId") Long userId, @Param("amount") BigDecimal amount, @Param("paymentMethod") String paymentMethod);
    Map<String, Object> getReviewByOrder(@Param("orderId") Long orderId);
    int insertReview(@Param("req") OrderDtos.ReviewReq req, @Param("userId") Long userId, @Param("driverId") Long driverId);
    List<Map<String, Object>> listReviews();
    int hideReview(@Param("id") Long id, @Param("hidden") Integer hidden);

    int updateDriverProfile(@Param("driverId") Long driverId, @Param("vehicleTypeId") Long vehicleTypeId,
                            @Param("vehiclePlate") String vehiclePlate, @Param("phone") String phone,
                            @Param("serviceArea") String serviceArea);
    Map<String, Object> driverIncome(@Param("driverId") Long driverId);

    List<Map<String, Object>> listUsers();
    List<Map<String, Object>> listDrivers(@Param("auditStatus") String auditStatus);
    Map<String, Object> dashboard();
    int insertAdminLog(@Param("adminId") Long adminId, @Param("action") String action, @Param("targetType") String targetType,
                       @Param("targetId") Long targetId, @Param("detail") String detail);
}
