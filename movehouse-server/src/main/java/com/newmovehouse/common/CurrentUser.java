package com.newmovehouse.common;

/**
 * 当前请求线程关联的登录用户快照（由 JWT 解析并可能合并数据库中的司机审核状态）。
 */
public class CurrentUser {
    /** 用户/司机/管理员主键 */
    public Long id;
    /** 角色：USER / DRIVER / ADMIN */
    public String role;
    /** 登录名 */
    public String username;
    /** 司机审核状态：PENDING / APPROVED / REJECTED 等，非司机可为 null */
    public String auditStatus;

    /**
     * @param id          主键
     * @param role        角色
     * @param username    用户名
     * @param auditStatus 司机审核状态
     */
    public CurrentUser(Long id, String role, String username, String auditStatus) {
        this.id = id;
        this.role = role;
        this.username = username;
        this.auditStatus = auditStatus;
    }
}
