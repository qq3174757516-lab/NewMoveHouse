package com.newmovehouse.websocket;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.newmovehouse.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.net.URI;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class MoveHouseWebSocketHandler extends TextWebSocketHandler {
    private final Map<Long, WebSocketSession> userSessions = new ConcurrentHashMap<>();
    private final Map<Long, WebSocketSession> driverSessions = new ConcurrentHashMap<>();
    private final Set<WebSocketSession> allSessions = Collections.newSetFromMap(new ConcurrentHashMap<WebSocketSession, Boolean>());

    @Autowired
    private JwtUtil jwtUtil;
    @Autowired
    private ObjectMapper objectMapper;

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        String token = tokenFromQuery(session.getUri());
        Map<String, Object> claims = jwtUtil.parse(token);
        Long id = Long.valueOf(String.valueOf(claims.get("id")));
        String role = String.valueOf(claims.get("role"));
        session.getAttributes().put("id", id);
        session.getAttributes().put("role", role);
        allSessions.add(session);
        if ("USER".equals(role)) {
            userSessions.put(id, session);
        } else if ("DRIVER".equals(role)) {
            driverSessions.put(id, session);
        }
        session.sendMessage(new TextMessage(objectMapper.writeValueAsString(message("CONNECTED", "连接成功"))));
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        allSessions.remove(session);
        Object id = session.getAttributes().get("id");
        Object role = session.getAttributes().get("role");
        if (id != null && "USER".equals(role)) {
            userSessions.remove(Long.valueOf(String.valueOf(id)));
        }
        if (id != null && "DRIVER".equals(role)) {
            driverSessions.remove(Long.valueOf(String.valueOf(id)));
        }
    }

    public void pushToUser(Long userId, String type, Object data) {
        send(userSessions.get(userId), type, data);
    }

    public void pushToDrivers(String type, Object data) {
        for (WebSocketSession session : driverSessions.values()) {
            send(session, type, data);
        }
    }

    public void pushToDriver(Long driverId, String type, Object data) {
        send(driverSessions.get(driverId), type, data);
    }

    private void send(WebSocketSession session, String type, Object data) {
        if (session == null || !session.isOpen()) {
            return;
        }
        synchronized (session) {
            try {
                session.sendMessage(new TextMessage(objectMapper.writeValueAsString(message(type, data))));
            } catch (Exception ignored) {
            }
        }
    }

    private Map<String, Object> message(String type, Object data) {
        Map<String, Object> map = new HashMap<>();
        map.put("type", type);
        map.put("data", data);
        map.put("time", System.currentTimeMillis());
        return map;
    }

    private String tokenFromQuery(URI uri) {
        if (uri == null || uri.getQuery() == null) {
            throw new IllegalArgumentException("missing token");
        }
        for (String part : uri.getQuery().split("&")) {
            String[] kv = part.split("=", 2);
            if (kv.length == 2 && "token".equals(kv[0])) {
                return kv[1];
            }
        }
        throw new IllegalArgumentException("missing token");
    }
}

