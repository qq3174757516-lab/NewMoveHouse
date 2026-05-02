package com.newmovehouse.entity;

import java.math.BigDecimal;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 与数据库表结构对应的简单 POJO（扁平字段，供 MyBatis 映射与缓存序列化）。
 */
public class Entities {
    /** 车型 */
    public static class VehicleType implements Serializable {
        public Long id;
        public String name;
        public String description;
        public BigDecimal basePrice;
        public BigDecimal perKmPrice;
        public BigDecimal loadCapacity;
        public Integer sortOrder;
        public Integer enabled;
    }

    /** 计价规则 */
    public static class PricingRule implements Serializable {
        public Long id;
        public String ruleKey;
        public BigDecimal ruleValue;
        public String description;
        public Integer enabled;
    }

    /** 司机实时位置（Redis JSON） */
    public static class DriverLocation implements Serializable {
        public Long driverId;
        public BigDecimal lng;
        public BigDecimal lat;
        public LocalDateTime reportTime;
    }

    /** 系统公告 */
    public static class Announcement implements Serializable {
        public Long id;
        public String audience;
        public String title;
        public String content;
        public Integer enabled;
        public LocalDateTime createdAt;
        public LocalDateTime updatedAt;
    }

    /** 司机资质文件记录 */
    public static class DriverDocument implements Serializable {
        public Long id;
        public Long driverId;
        public String docType;
        public String fileUrl;
        public String originalName;
        public LocalDateTime createdAt;
    }
}
