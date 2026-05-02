package com.newmovehouse.dto;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public class OrderDtos {
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

    public static class CreateOrderReq extends EstimateReq {
        public Long id;
        @NotBlank public String startAddress;
        @NotBlank public String endAddress;
        public String itemDescription;
        @NotBlank public String contactName;
        @NotBlank public String contactPhone;
    }

    public static class ReviewReq {
        @NotNull public Long orderId;
        @NotNull public Integer rating;
        public String content;
    }

    public static class CancelReq {
        @NotNull public Long orderId;
        public String reason;
    }
}
