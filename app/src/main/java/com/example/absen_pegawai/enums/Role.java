package com.example.absen_pegawai.enums;

import java.util.HashMap;
import java.util.Map;

public final class Role {
    public static final String ADMIN = "Admin";
    public static final String USER = "User";

    private static final Map<String, Role> roleMap = new HashMap<>();

    static {
        roleMap.put(ADMIN, new Role(ADMIN));
        roleMap.put(USER, new Role(USER));
    }

    private final String roleName;

    private Role(String roleName) {
        this.roleName = roleName;
    }

    public String getRoleName() {
        return roleName;
    }

    public static Role getRoleByName(String roleName) {
        return roleMap.get(roleName);
    }
}
