package com.example.base.common.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum UserStatus {
    DISABLED(0),
    ACTIVE(1),
    LOCKED(2);

    @EnumValue
    private final int code;

    public static UserStatus fromCode(Integer code) {
        if (code == null) {
            return ACTIVE;
        }
        for (UserStatus status : values()) {
            if (status.code == code) {
                return status;
            }
        }
        return ACTIVE;
    }
}
