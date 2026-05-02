package com.newmovehouse.dto;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

/** 管理端请求体：车型、计价规则、司机审核、公告、投诉处理等。 */
public class AdminDtos {
    /** 车型维护 */
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

    /** 计价规则维护 */
    public static class PricingRuleReq {
        @NotBlank public String ruleKey;
        @NotNull public BigDecimal ruleValue;
        public String description;
        public Integer enabled = 1;
    }

    /** 司机审核 */
    public static class AuditDriverReq {
        @NotNull public Long driverId;
        @NotBlank public String status;
        public String reason;
    }

    /** 公告发布 */
    public static class AnnouncementReq {
        public Long id;
        @NotBlank public String audience;
        @NotBlank public String title;
        @NotBlank public String content;
        public Integer enabled = 1;
    }

    /** 投诉处理 */
    public static class ComplaintUpdateReq {
        @NotBlank public String status;
        public String adminRemark;
        public String actionType = "NONE";
        public BigDecimal penaltyAmount;
    }
}
