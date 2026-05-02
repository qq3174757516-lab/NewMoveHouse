package com.newmovehouse.dto;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

public class AuthDtos {
    public static class LoginReq {
        @NotBlank public String username;
        @NotBlank public String password;
        @NotBlank public String role;
    }

    public static class RegisterUserReq {
        public Long id;
        @NotBlank public String username;
        @NotBlank public String password;
        @NotBlank public String phone;
        public String nickname;
    }

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

    public static class AddressReq {
        public Long id;
        @NotBlank public String name;
        @NotBlank public String detail;
        @NotNull public BigDecimal lng;
        @NotNull public BigDecimal lat;
        public String contactName;
        public String contactPhone;
    }
}
