package com.newmovehouse.dto;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/** 订单相关请求：估价、下单、评价、投诉、取消。 */
public class OrderDtos {
    /** 估价/计价入参（与下单字段共用坐标与楼层等） */
    public static class EstimateReq {
        @NotNull public Long vehicleTypeId;
        @NotNull public BigDecimal startLng;
        @NotNull public BigDecimal startLat;
        @NotNull public BigDecimal endLng;
        @NotNull public BigDecimal endLat;
        public Boolean startHasElevator = true;
        public Boolean endHasElevator = true;
        public Integer startFloor = 1;
        public Integer endFloor = 1;
        public Integer largeItemCount = 0;
        public LocalDateTime appointmentTime;
    }

    /** 创建订单请求 */
    public static class CreateOrderReq extends EstimateReq {
        public Long id;
        @NotBlank public String startAddress;
        @NotBlank public String endAddress;
        public String itemDescription;
        @NotBlank public String contactName;
        @NotBlank public String contactPhone;
    }

    /** 评价 */
    public static class ReviewReq {
        @NotNull public Long orderId;
        @NotNull public Integer rating;
        public String content;
    }

    /** 投诉 */
    public static class ComplaintReq {
        @NotNull public Long orderId;
        @NotBlank public String title;
        @NotBlank public String content;
        @NotBlank public String imageUrl;
    }

    /** 取消订单（部分接口仅用 reason 字段） */
    public static class CancelReq {
        @NotNull public Long orderId;
        public String reason;
    }
}
