package com.example.web.annotation;

import java.lang.annotation.*;

/**
 * 标注在 Controller 类或方法上，禁用统一返回包装。
 */
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface NoWrap {}
