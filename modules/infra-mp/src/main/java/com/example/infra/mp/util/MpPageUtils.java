package com.example.infra.mp.util;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.example.common.model.PageResult;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * MyBatis-Plus 分页对象与通用 PageResult 的转换。
 */
public final class MpPageUtils {
    private MpPageUtils(){}

    public static <T> PageResult<T> toPageResult(IPage<T> page) {
        return PageResult.of((int) page.getCurrent(), (int) page.getSize(), page.getTotal(), page.getRecords());
    }

    public static <T, R> PageResult<R> toPageResult(IPage<T> page, Function<T, R> mapper) {
        List<R> items = page.getRecords().stream().map(mapper).collect(Collectors.toList());
        return PageResult.of((int) page.getCurrent(), (int) page.getSize(), page.getTotal(), items);
    }
}
