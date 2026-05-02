package com.newmovehouse.entity;

import java.math.BigDecimal;
import java.io.Serializable;
import java.time.LocalDateTime;

public class Entities {
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

    public static class PricingRule implements Serializable {
        public Long id;
        public String ruleKey;
        public BigDecimal ruleValue;
        public String description;
        public Integer enabled;
    }

    public static class DriverLocation implements Serializable {
        public Long driverId;
        public BigDecimal lng;
        public BigDecimal lat;
        public LocalDateTime reportTime;
    }
}
