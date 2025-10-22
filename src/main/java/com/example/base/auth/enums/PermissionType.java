package com.example.base.auth.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum PermissionType {
    API("api"),
    MENU("menu"),
    DATA("data");

    @EnumValue
    private final String code;

    public static PermissionType fromCode(String code) {
        if (code == null) {
            return API;
        }
        for (PermissionType type : values()) {
            if (type.code.equalsIgnoreCase(code)) {
                return type;
            }
        }
        return API;
    }
}
