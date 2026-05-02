package com.newmovehouse.service;

import com.newmovehouse.common.BizException;
import com.newmovehouse.common.UserContext;
import com.newmovehouse.dto.OrderDtos;
import com.newmovehouse.mapper.AppMapper;
import com.newmovehouse.util.GeoUtil;
import com.newmovehouse.websocket.MoveHouseWebSocketHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 订单领域服务：下单、列表、接单大厅、司机状态推进、支付、取消、评价与投诉等业务逻辑。
 */
@Service
public class OrderService {
    @Autowired
    private AppMapper mapper;
    @Autowired
    private PricingService pricingService;
    @Autowired
    private MoveHouseWebSocketHandler ws;
    @Autowired
    private StringRedisTemplate redisTemplate;
    @Autowired
    private ObjectMapper objectMapper;

    /**
     * 用户创建订单，写入数据库与状态日志，并向司机端广播新订单。
     *
     * @param req 下单请求体（含车型、起终点、楼层等）
     * @return 新建订单详情（含 id 等字段）
     */
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

    /**
     * 查询订单详情并附带状态流转日志。
     *
     * @param id 订单主键
     * @return 订单 Map，含 logs
     */
    public Map<String, Object> detail(Long id) {
        Map<String, Object> order = mapper.getOrder(id);
        if (order == null) {
            throw new BizException("订单不存在");
        }
        order.put("logs", mapper.listOrderLogs(id));
        return order;
    }

    /**
     * 当前登录用户的订单列表。
     *
     * @param status 可选状态筛选，可为 null 表示全部
     */
    public List<Map<String, Object>> userOrders(String status) {
        return mapper.listUserOrders(UserContext.id(), status);
    }

    /**
     * 当前登录司机的订单列表。
     *
     * @param status 可选状态筛选
     */
    public List<Map<String, Object>> driverOrders(String status) {
        return mapper.listDriverOrders(UserContext.id(), status);
    }

    /**
     * 接单大厅：待抢单列表，仅审核通过的司机可访问。
     */
    public List<Map<String, Object>> hall() {
        requireApprovedDriver();
        return mapper.listHallOrders();
    }

    /**
     * 司机抢单：将订单从待接单更新为已接单，并推送 WebSocket 通知。
     *
     * @param orderId 订单 id
     * @return 更新后的订单详情
     */
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

    /**
     * 司机推进订单状态（到达装货点、开始搬运、完成搬运等）。
     *
     * @param orderId 订单 id
     * @param action  动作枚举：ARRIVE / START_MOVE / FINISH_MOVE
     * @return 更新后的订单详情
     */
    public Map<String, Object> nextStatus(Long orderId, String action) {
        Long driverId = UserContext.id();
        Map<String, Object> order = detail(orderId);
        if (order.get("driverId") == null || !driverId.equals(Long.valueOf(String.valueOf(order.get("driverId"))))) {
            throw new BizException("只能操作自己的订单");
        }
        String current = String.valueOf(order.get("status"));
        String next = next(current, action);
        String locationRemark = null;
        if ("ARRIVE".equals(action)) {
            locationRemark = requireDriverLocation(driverId);
        }

        //TODO
        // 临时关闭：完成搬运时要求司机在终点附近（约 500m）的限制，便于演示与调试；恢复校验时取消下面注释并调用 requireNearOrderEnd

        // if ("FINISH_MOVE".equals(action)) {
        //     requireNearOrderEnd(driverId, order);
        // }

        if (mapper.updateOrderStatus(orderId, current, next) == 0) {
            throw new BizException("订单状态已变化");
        }
        String remark = "司机操作：" + action + (locationRemark == null ? "" : "，当前位置：" + locationRemark);
        mapper.insertStatusLog(orderId, "DRIVER", driverId, current, next, remark);
        Map<String, Object> updated = detail(orderId);
        ws.pushToUser(Long.valueOf(String.valueOf(updated.get("userId"))), "ORDER_STATUS_CHANGED", updated);
        return updated;
    }

    /**
     * 用户模拟支付：记录支付流水、更新订单为已完成，并通知司机。
     *
     * @param orderId        订单 id
     * @param paymentMethod 支付方式（WECHAT_PAY / ALIPAY）
     * @return 支付完成后的订单详情
     */
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

    /**
     * 取消订单：用户/司机走受限 SQL，管理员可走强制取消。
     *
     * @param orderId    订单 id
     * @param reason     取消原因
     * @param role       操作方角色标识（USER / DRIVER / ADMIN）
     * @param operatorId 操作人 id（管理员场景）
     * @return 取消后的订单详情
     */
    public Map<String, Object> cancel(Long orderId, String reason, String role, Long operatorId) {
        Map<String, Object> order = detail(orderId);
        String old = String.valueOf(order.get("status"));
        int changed = "ADMIN".equals(role) ? mapper.cancelOrderAdmin(orderId, reason) : mapper.cancelOrder(orderId, reason);
        if (changed == 0) {
            throw new BizException("当前状态不可取消（用户仅待接单或已接单可取消）");
        }
        mapper.insertStatusLog(orderId, role, operatorId, old, "CANCELED", reason);
        Map<String, Object> updated = detail(orderId);
        ws.pushToUser(Long.valueOf(String.valueOf(updated.get("userId"))), "ORDER_STATUS_CHANGED", updated);
        if (updated.get("driverId") != null) {
            ws.pushToDriver(Long.valueOf(String.valueOf(updated.get("driverId"))), "ORDER_STATUS_CHANGED", updated);
        }
        return updated;
    }

    /**
     * 用户对已完成订单提交评价。
     *
     * @param req 评价请求
     */
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

    /**
     * 用户发起投诉（订单需已完成且已分配司机）。
     *
     * @param req 投诉标题、内容、图片等
     */
    public void complaint(OrderDtos.ComplaintReq req) {
        Long userId = UserContext.id();
        Map<String, Object> order = detail(req.orderId);
        if (!userId.equals(Long.valueOf(String.valueOf(order.get("userId"))))) {
            throw new BizException("只能投诉自己的订单");
        }
        if (order.get("driverId") == null) {
            throw new BizException("订单尚未分配司机，无法投诉");
        }
        if (!"COMPLETED".equals(String.valueOf(order.get("status")))) {
            throw new BizException("订单完成后才能发起投诉");
        }
        if (mapper.getComplaintByOrder(req.orderId) != null) {
            throw new BizException("该订单已提交过投诉");
        }
        mapper.insertComplaint(req.orderId, userId, Long.valueOf(String.valueOf(order.get("driverId"))), req.title, req.content, req.imageUrl);
    }

    /**
     * 当前用户提交的投诉列表。
     */
    public List<Map<String, Object>> userComplaints() {
        return mapper.listUserComplaints(UserContext.id());
    }

    /**
     * 当前司机被投诉的工单列表。
     */
    public List<Map<String, Object>> driverComplaints() {
        return mapper.listDriverComplaints(UserContext.id());
    }

    /**
     * 根据当前状态与动作解析下一状态，非法跳转会抛出业务异常。
     *
     * @param current 当前订单状态
     * @param action  司机动作
     * @return 下一状态枚举值
     */
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

    /** 校验当前线程用户为已审核通过的司机。 */
    private void requireApprovedDriver() {
        if (!"DRIVER".equals(UserContext.role())) {
            throw new BizException("司机端接口");
        }
        if (!"APPROVED".equals(UserContext.get().auditStatus)) {
            throw new BizException("司机审核通过后才能接单");
        }
    }

    /**
     * 校验司机当前位置是否在订单终点约 500m 内（从 Redis 读取最近一次上报）。
     * 当前业务上可由 {@link #nextStatus(Long, String)} 选择性调用。
     */
    private void requireNearOrderEnd(Long driverId, Map<String, Object> order) {
        Object endLngObj = order.get("endLng");
        Object endLatObj = order.get("endLat");
        if (endLngObj == null || endLatObj == null) {
            throw new BizException("订单终点坐标缺失，无法完成订单");
        }
        String val = redisTemplate.opsForValue().get("driver:location:" + driverId);
        if (val == null) {
            throw new BizException("请先上报当前位置，再完成订单");
        }
        try {
            // ProfileService 存储的是 DriverLocation JSON，其中包含 lng/lat
            // 这里不强依赖具体类，按 Map 解析即可
            Map map = objectMapper.readValue(val, Map.class);
            Object lngObj = map.get("lng");
            Object latObj = map.get("lat");
            if (lngObj == null || latObj == null) {
                throw new BizException("司机位置数据异常，请重新上报");
            }
            double driverLng = Double.parseDouble(String.valueOf(lngObj));
            double driverLat = Double.parseDouble(String.valueOf(latObj));
            double endLng = Double.parseDouble(String.valueOf(endLngObj));
            double endLat = Double.parseDouble(String.valueOf(endLatObj));
            double meters = GeoUtil.distanceMeters(driverLng, driverLat, endLng, endLat);
            if (meters > 500.0) {
                throw new BizException("当前不在终点附近（距离约" + Math.round(meters) + "m），到达终点500m内才能完成订单");
            }
        } catch (BizException e) {
            throw e;
        } catch (Exception e) {
            throw new BizException("位置校验失败，请重新上报位置后再试");
        }
    }

    /**
     * 到达装货点时要求司机已上报位置，返回经纬度字符串写入状态备注。
     *
     * @param driverId 司机 id
     * @return "lng,lat" 文本
     */
    private String requireDriverLocation(Long driverId) {
        String val = redisTemplate.opsForValue().get("driver:location:" + driverId);
        if (val == null) {
            throw new BizException("请先上报当前位置，再确认到达");
        }
        try {
            Map map = objectMapper.readValue(val, Map.class);
            Object lngObj = map.get("lng");
            Object latObj = map.get("lat");
            if (lngObj == null || latObj == null) {
                throw new BizException("司机位置数据异常，请重新上报");
            }
            return lngObj + "," + latObj;
        } catch (BizException e) {
            throw e;
        } catch (Exception e) {
            throw new BizException("司机位置读取失败，请重新上报");
        }
    }
}
