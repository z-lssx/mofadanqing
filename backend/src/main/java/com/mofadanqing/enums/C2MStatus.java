package com.mofadanqing.enums;

public enum C2MStatus {
    WAIT_C2M(0, "待C2M定制", "wait", "USER"),
    C2M_CONFIRMED(1, "C2M定制", "confirmed", "USER"), 
    MATERIAL_SENT(2, "采发包", "material", "ADMIN"),
    WORKSHOP_RECEIVED(3, "工坊签收", "workshop", "ADMIN"),
    IN_PRODUCTION(4, "绣制中", "producing", "ADMIN"),
    SHIPPED(5, "成品发货", "shipped", "ADMIN"),
    RECEIVED(6, "用户签收", "received", "USER");

    private final int code;
    private final String description;
    private final String alias;
    private final String roleAllowed; // USER or ADMIN

    C2MStatus(int code, String description, String alias, String roleAllowed) {
        this.code = code;
        this.description = description;
        this.alias = alias;
        this.roleAllowed = roleAllowed;
    }

    public int getCode() { return code; }
    public String getDescription() { return description; }
    public String getAlias() { return alias; }
    public String getRoleAllowed() { return roleAllowed; }

    public static C2MStatus fromAlias(String alias) {
        for (C2MStatus status : values()) {
            if (status.alias.equalsIgnoreCase(alias)) {
                return status;
            }
        }
        return null;
    }

    public static C2MStatus fromDescription(String description) {
        for (C2MStatus status : values()) {
            if (status.description.equals(description)) {
                return status;
            }
        }
        return null;
    }

    // 检查状态流转是否合法（必须是下一级）
    public boolean canTransitionTo(C2MStatus next) {
        return next.code == this.code + 1;
    }
}
