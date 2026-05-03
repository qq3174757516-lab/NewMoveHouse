package com.newmovehouse.util;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 高德 Web 服务 REST：逆地理编码、关键字搜索（不经过浏览器 JSAPI，可避免 INVALID_USER_SCODE）。
 * Key 需在高德控制台勾选「Web 服务」且具备地理编码/搜索等权限；可与路径规划共用 {@link #geoKey}。
 */
@Component
public class AmapGeoRestClient {
    @Autowired
    private RestTemplate restTemplate;

    @Value("${amap.server-key:}")
    private String serverKey;
    /** 若与路径规划 Key 不同，单独配置；为空则使用 server-key */
    @Value("${amap.geo-key:}")
    private String geoKeyOverride;

/*    private String geoKey() {
        if (geoKeyOverride != null && !geoKeyOverride.isBlank() && !geoKeyOverride.startsWith("your_")) {
            return geoKeyOverride.trim();
        }
        return serverKey == null ? "" : serverKey.trim();
    }*/

    // ... existing code ...
    private String geoKey() {
        if (geoKeyOverride != null && !geoKeyOverride.trim().isEmpty() && !geoKeyOverride.startsWith("your_")) {
            return geoKeyOverride.trim();
        }
        return serverKey == null ? "" : serverKey.trim();
    }



    /**
     * 逆地理编码：/v3/geocode/regeo
     *
     * @return success、displayAddress（用于界面展示）、formattedAddress、lng、lat、info/infocode（失败时）
     */
    @SuppressWarnings("unchecked")
    public Map<String, Object> reverseGeocode(BigDecimal lng, BigDecimal lat) {
        Map<String, Object> out = new HashMap<>();
        String key = geoKey();
        if (key.isEmpty() || key.startsWith("your_")) {
            out.put("success", false);
            out.put("message", "未配置 amap.server-key 或 amap.geo-key（需具备逆地理编码权限的 Web 服务 Key）");
            return out;
        }
        try {
            String url = "https://restapi.amap.com/v3/geocode/regeo?key={key}&location={loc}&radius=1000&extensions=all";
            Map<?, ?> res = restTemplate.getForObject(url, Map.class, key, lng + "," + lat);
            if (res == null) {
                out.put("success", false);
                out.put("message", "高德逆地理无响应");
                return out;
            }
            if (!"1".equals(String.valueOf(res.get("status")))) {
                out.put("success", false);
                out.put("message", String.valueOf(res.get("info")));
                out.put("infocode", res.get("infocode"));
                return out;
            }
            Map<?, ?> regeocode = (Map<?, ?>) res.get("regeocode");
            String formatted = "";
            if (regeocode != null && regeocode.get("formatted_address") != null) {
                formatted = String.valueOf(regeocode.get("formatted_address"));
            }
            String poiName = null;
            if (regeocode != null && regeocode.get("pois") instanceof List) {
                List<?> pois = (List<?>) regeocode.get("pois");
                if (pois != null && !pois.isEmpty() && pois.get(0) instanceof Map) {
                    Map<?, ?> p = (Map<?, ?>) pois.get(0);
                    if (p.get("name") != null) {
                        poiName = String.valueOf(p.get("name"));
                    }
                }
            }
            String display;
            if (poiName != null && !poiName.isEmpty() && !formatted.isEmpty()) {
                display = poiName + " · " + formatted;
            } else if (!formatted.isEmpty()) {
                display = formatted;
            } else if (poiName != null) {
                display = poiName;
            } else {
                display = lng + "," + lat;
            }
            out.put("success", true);
            out.put("displayAddress", display);
            out.put("formattedAddress", formatted);
            out.put("lng", lng);
            out.put("lat", lat);
            return out;
        } catch (Exception e) {
            out.put("success", false);
            out.put("message", "逆地理请求失败：" + e.getMessage());
            return out;
        }
    }

    /**
     * 关键字搜索第一条结果：/v3/place/text
     *
     * @return success、name、address、displayAddress、lng、lat
     */
    @SuppressWarnings("unchecked")
    public Map<String, Object> placeTextSearch(String keywords) {
        Map<String, Object> out = new HashMap<>();
        String key = geoKey();
        if (keywords == null || keywords.trim().isEmpty()) {
            out.put("success", false);
            out.put("message", "关键词为空");
            return out;
        }
        if (key.isEmpty() || key.startsWith("your_")) {
            out.put("success", false);
            out.put("message", "未配置 amap.server-key 或 amap.geo-key（需具备关键字搜索权限的 Web 服务 Key）");
            return out;
        }
        try {
            String url = "https://restapi.amap.com/v3/place/text?key={key}&keywords={keywords}&citylimit=false&offset=1&page=1&extensions=all";
            Map<?, ?> res = restTemplate.getForObject(url, Map.class, key, keywords.trim());
            if (res == null) {
                out.put("success", false);
                out.put("message", "高德搜索无响应");
                return out;
            }
            if (!"1".equals(String.valueOf(res.get("status")))) {
                out.put("success", false);
                out.put("message", String.valueOf(res.get("info")));
                out.put("infocode", res.get("infocode"));
                return out;
            }
            List<?> pois = (List<?>) res.get("pois");
            if (pois == null || pois.isEmpty()) {
                out.put("success", false);
                out.put("message", "未找到地点");
                return out;
            }
            Map<?, ?> poi = (Map<?, ?>) pois.get(0);
            String locStr = poi.get("location") == null ? null : String.valueOf(poi.get("location"));
            if (locStr == null || !locStr.contains(",")) {
                out.put("success", false);
                out.put("message", "搜索结果无坐标");
                return out;
            }
            String[] parts = locStr.split(",");
            BigDecimal glng = new BigDecimal(parts[0].trim());
            BigDecimal glat = new BigDecimal(parts[1].trim());
            String name = poi.get("name") == null ? "" : String.valueOf(poi.get("name"));
            String addr = poi.get("address") == null ? "" : String.valueOf(poi.get("address"));
            String display = name.isEmpty() ? addr : (addr.isEmpty() ? name : name + " · " + addr);
            out.put("success", true);
            out.put("name", name);
            out.put("address", addr);
            out.put("displayAddress", display);
            out.put("lng", glng);
            out.put("lat", glat);
            return out;
        } catch (Exception e) {
            out.put("success", false);
            out.put("message", "地点搜索失败：" + e.getMessage());
            return out;
        }
    }
}
