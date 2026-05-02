package com.newmovehouse.util;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Map;

/**
 * 高德驾车路径规划 REST 封装；无有效 Key 或请求失败时回退为直线距离 × 系数估算。
 */
@Component
public class AmapClient {
    @Value("${amap.server-key:}")
    private String key;
    @Autowired
    private RestTemplate restTemplate;

    /**
     * 查询驾车导航距离（公里），失败或无 Key 时使用 Haversine×1.25 近似。
     */
    public BigDecimal drivingDistanceKm(BigDecimal startLng, BigDecimal startLat, BigDecimal endLng, BigDecimal endLat) {
        if (key != null && key.trim().length() > 0 && !key.startsWith("your_")) {
            try {
                String url = "https://restapi.amap.com/v3/direction/driving?origin={origin}&destination={destination}&key={key}";
                Map<?, ?> res = restTemplate.getForObject(url, Map.class,
                        startLng + "," + startLat, endLng + "," + endLat, key);
                Map<?, ?> route = (Map<?, ?>) res.get("route");
                List<?> paths = (List<?>) route.get("paths");
                if (paths != null && !paths.isEmpty()) {
                    Map<?, ?> path = (Map<?, ?>) paths.get(0);
                    BigDecimal meters = new BigDecimal(String.valueOf(path.get("distance")));
                    return meters.divide(new BigDecimal("1000"), 2, RoundingMode.HALF_UP);
                }
            } catch (Exception ignored) {
                // Fall back to straight-line estimate for local demo without a valid Amap key/network.
            }
        }
        return haversineKm(startLng, startLat, endLng, endLat).multiply(new BigDecimal("1.25")).setScale(2, RoundingMode.HALF_UP);
    }

    /** 地球大圆近似距离（公里） */
    private BigDecimal haversineKm(BigDecimal lng1, BigDecimal lat1, BigDecimal lng2, BigDecimal lat2) {
        double r = 6371.0;
        double dLat = Math.toRadians(lat2.doubleValue() - lat1.doubleValue());
        double dLng = Math.toRadians(lng2.doubleValue() - lng1.doubleValue());
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1.doubleValue())) * Math.cos(Math.toRadians(lat2.doubleValue()))
                * Math.sin(dLng / 2) * Math.sin(dLng / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return BigDecimal.valueOf(r * c).setScale(2, RoundingMode.HALF_UP);
    }
}
