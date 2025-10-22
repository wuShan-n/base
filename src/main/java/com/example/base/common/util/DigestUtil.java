package com.example.base.common.util;

import cn.hutool.crypto.SecureUtil;

public final class DigestUtil {

    private DigestUtil() {
    }

    public static String sha256Hex(String value) {
        return SecureUtil.sha256(value);
    }
}
