package com.newmovehouse.dto;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

/** 认证与用户资料相关请求体（登录、注册、地址、资料更新）。 */
public class AuthDtos {
    /** 登录请求 */
    public static class LoginReq {
        @NotBlank public String username;
        @NotBlank public String password;
        @NotBlank public String role;
    }

    /** 用户注册请求 */
    public static class RegisterUserReq {
        public Long id;
        @NotBlank public String username;
        @NotBlank public String password;
        @NotBlank public String phone;
        public String nickname;
    }

    /** 司机注册请求 */
    public static class RegisterDriverReq {
        public Long id;
        @NotBlank public String username;
        @NotBlank public String password;
        @NotBlank public String phone;
        @NotBlank public String realName;
        @NotBlank public String vehiclePlate;
        @NotNull public Long vehicleTypeId;
        public String serviceArea;
    }

    /** 常用地址新增/修改 */
    public static class AddressReq {
        public Long id;
        @NotBlank public String name;
        @NotBlank public String detail;
        @NotNull public BigDecimal lng;
        @NotNull public BigDecimal lat;
        public String contactName;
        public String contactPhone;
    }

    /** 用户资料更新 */
    public static class UpdateUserProfileReq {
        @NotBlank public String phone;
        public String nickname;
    }

    /** 司机资料全量更新 */
    public static class UpdateDriverProfileReq {
        @NotBlank public String realName;
        @NotBlank public String phone;
        @NotNull public Long vehicleTypeId;
        @NotBlank public String vehiclePlate;
        public String serviceArea;
    }
}
