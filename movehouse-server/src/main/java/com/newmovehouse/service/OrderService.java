package com.newmovehouse.service;

import com.newmovehouse.common.BizException;
import com.newmovehouse.common.UserContext;
import com.newmovehouse.dto.OrderDtos;
import com.newmovehouse.mapper.AppMapper;
import com.newmovehouse.websocket.MoveHouseWebSocketHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class OrderService {
    @Autowired
    private AppMapper mapper;
    @Autowired
    private PricingService pricingService;
    @Autowired
    private MoveHouseWebSocketHandler ws;

    public Map<String, Object> create(OrderDtos.CreateOrderReq req) {
        Long userId = UserContext.id();
        BigDecimal distance = pricingService.distanceKm(req);
        BigDecimal amount = pricingService.estimateAmount(req);
        mapper.insertOrder(req, userId, distance, amount);
        mapper.insertStatusLog(req.id, "USER", userId, null, "WAITING_ACCEPT", "用户创建订单");
        Map<String, Object> order = detail(req.id);
        ws.pushToDrivers("NEW_ORDER", order);
        return order;
    }

    public Map<String, Object> detail(Long id) {
        Map<String, Object> order = mapper.getOrder(id);
        if (order == null) {
            throw new BizException("订单不存在");
        }
        order.put("logs", mapper.listOrderLogs(id));
        return order;
    }

    public List<Map<String, Object>> userOrders(String status) {
        return mapper.listUserOrders(UserContext.id(), status);
    }

    public List<Map<String, Object>> driverOrders(String status) {
        return mapper.listDriverOrders(UserContext.id(), status);
    }

    public List<Map<String, Object>> hall() {
        requireApprovedDriver();
        return mapper.listHallOrders();
    }

    public Map<String, Object> accept(Long orderId) {
        requireApprovedDriver();
        Long driverId = UserContext.id();
        if (mapper.acceptOrder(orderId, driverId) == 0) {
            throw new BizException("订单已被接走或状态不可接单");
        }
        Map<String, Object> order = detail(orderId);
        mapper.insertStatusLog(orderId, "DRIVER", driverId, "WAITING_ACCEPT", "ACCEPTED", "司机抢单");
        ws.pushToUser(Long.valueOf(String.valueOf(order.get("userId"))), "ORDER_STATUS_CHANGED", order);
        ws.pushToDrivers("ORDER_ACCEPTED", order);
        return order;
    }

    public Map<String, Object> nextStatus(Long orderId, String action) {
        Long driverId = UserContext.id();
        Map<String, Object> order = detail(orderId);
        if (order.get("driverId") == null || !driverId.equals(Long.valueOf(String.valueOf(order.get("driverId"))))) {
            throw new BizException("只能操作自己的订单");
        }
        String current = String.valueOf(order.get("status"));
        String next = next(current, action);
        if (mapper.updateOrderStatus(orderId, current, next) == 0) {
            throw new BizException("订单状态已变化");
        }
        mapper.insertStatusLog(orderId, "DRIVER", driverId, current, next, "司机操作：" + action);
        Map<String, Object> updated = detail(orderId);
        ws.pushToUser(Long.valueOf(String.valueOf(updated.get("userId"))), "ORDER_STATUS_CHANGED", updated);
        return updated;
    }

    @Transactional
    public Map<String, Object> pay(Long orderId, String paymentMethod) {
        Long userId = UserContext.id();
        Map<String, Object> order = detail(orderId);
        if (!userId.equals(Long.valueOf(String.valueOf(order.get("userId"))))) {
            throw new BizException("只能支付自己的订单");
        }
        String method = paymentMethod == null ? "WECHAT_PAY" : paymentMethod;
        if (!"WECHAT_PAY".equals(method) && !"ALIPAY".equals(method)) {
            throw new BizException("支付方式不支持");
        }
        mapper.insertPayment(orderId, userId, new BigDecimal(String.valueOf(order.get("finalAmount"))), method);
        if (mapper.payOrder(orderId) == 0) {
            throw new BizException("订单未到待支付状态");
        }
        mapper.insertStatusLog(orderId, "USER", userId, "MOVED", "COMPLETED", "模拟支付完成");
        Map<String, Object> updated = detail(orderId);
        if (updated.get("driverId") != null) {
            ws.pushToDriver(Long.valueOf(String.valueOf(updated.get("driverId"))), "ORDER_STATUS_CHANGED", updated);
        }
        return updated;
    }

    public Map<String, Object> cancel(Long orderId, String reason, String role, Long operatorId) {
        Map<String, Object> order = detail(orderId);
        String old = String.valueOf(order.get("status"));
        mapper.cancelOrder(orderId, reason);
        mapper.insertStatusLog(orderId, role, operatorId, old, "CANCELED", reason);
        Map<String, Object> updated = detail(orderId);
        ws.pushToUser(Long.valueOf(String.valueOf(updated.get("userId"))), "ORDER_STATUS_CHANGED", updated);
        if (updated.get("driverId") != null) {
            ws.pushToDriver(Long.valueOf(String.valueOf(updated.get("driverId"))), "ORDER_STATUS_CHANGED", updated);
        }
        return updated;
    }

    public void review(OrderDtos.ReviewReq req) {
        Long userId = UserContext.id();
        Map<String, Object> order = detail(req.orderId);
        if (!"COMPLETED".equals(String.valueOf(order.get("status")))) {
            throw new BizException("完成后才能评价");
        }
        if (!userId.equals(Long.valueOf(String.valueOf(order.get("userId"))))) {
            throw new BizException("只能评价自己的订单");
        }
        if (mapper.getReviewByOrder(req.orderId) != null) {
            throw new BizException("订单已评价");
        }
        mapper.insertReview(req, userId, Long.valueOf(String.valueOf(order.get("driverId"))));
    }

    private String next(String current, String action) {
        Map<String, String> map = new HashMap<>();
        map.put("ARRIVE", "ACCEPTED:ARRIVED_LOADING");
        map.put("START_MOVE", "ARRIVED_LOADING:MOVING");
        map.put("FINISH_MOVE", "MOVING:MOVED");
        String spec = map.get(action);
        if (spec == null) {
            throw new BizException("未知操作");
        }
        String[] arr = spec.split(":");
        if (!arr[0].equals(current)) {
            throw new BizException("订单状态不可跳步");
        }
        return arr[1];
    }

    private void requireApprovedDriver() {
        if (!"DRIVER".equals(UserContext.role())) {
            throw new BizException("司机端接口");
        }
        if (!"APPROVED".equals(UserContext.get().auditStatus)) {
            throw new BizException("司机审核通过后才能接单");
        }
    }
}
