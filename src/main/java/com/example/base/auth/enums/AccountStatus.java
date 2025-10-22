package com.example.base.auth.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum AccountStatus {
    DISABLED(0),
    ACTIVE(1),
    LOCKED(2);

    @EnumValue
    private final int code;

    public static AccountStatus fromCode(Integer code) {
        if (code == null) {
            return ACTIVE;
        }
        for (AccountStatus status : values()) {
            if (status.code == code) {
                return status;
            }
        }
        return ACTIVE;
    }
}
