package com.newmovehouse.util;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

/**
 * 密码哈希与比对（SHA-256 十六进制），用于注册与登录校验。
 */
public class PasswordUtil {

    /**
     * 对明文密码做 SHA-256 并转为小写十六进制字符串。
     *
     * @param raw 明文
     * @return 哈希串
     */
    public static String hash(String raw) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] bytes = md.digest(raw.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : bytes) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    /**
     * 校验明文是否与已存哈希一致（忽略大小写）。
     */
    public static boolean matches(String raw, String hashed) {
        return hash(raw).equalsIgnoreCase(hashed);
    }
}
