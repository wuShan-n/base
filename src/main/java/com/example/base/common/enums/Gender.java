package com.example.base.common.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum Gender {
    UNKNOWN(0),
    MALE(1),
    FEMALE(2),
    OTHER(9);

    @EnumValue
    private final int code;

    public static Gender fromCode(Integer code) {
        if (code == null) {
            return UNKNOWN;
        }
        for (Gender gender : values()) {
            if (gender.code == code) {
                return gender;
            }
        }
        return UNKNOWN;
    }
}
