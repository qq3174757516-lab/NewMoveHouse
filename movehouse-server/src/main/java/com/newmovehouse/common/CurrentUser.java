package com.newmovehouse.common;

public class CurrentUser {
    public Long id;
    public String role;
    public String username;
    public String auditStatus;

    public CurrentUser(Long id, String role, String username, String auditStatus) {
        this.id = id;
        this.role = role;
        this.username = username;
        this.auditStatus = auditStatus;
    }
}

