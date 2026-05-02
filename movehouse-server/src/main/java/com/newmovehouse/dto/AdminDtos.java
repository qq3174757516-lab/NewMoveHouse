package com.newmovehouse.dto;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

public class AdminDtos {
    public static class VehicleTypeReq {
        public Long id;
        @NotBlank public String name;
        public String description;
        @NotNull public BigDecimal basePrice;
        @NotNull public BigDecimal perKmPrice;
        public BigDecimal loadCapacity;
        public Integer sortOrder = 0;
        public Integer enabled = 1;
    }

    public static class PricingRuleReq {
        @NotBlank public String ruleKey;
        @NotNull public BigDecimal ruleValue;
        public String description;
        public Integer enabled = 1;
    }

    public static class AuditDriverReq {
        @NotNull public Long driverId;
        @NotBlank public String status;
        public String reason;
    }
}
