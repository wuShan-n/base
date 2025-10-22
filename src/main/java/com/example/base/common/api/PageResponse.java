package com.example.base.common.api;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Collections;
import java.util.List;

@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class PageResponse<T> {
    private long pageNo;
    private long pageSize;
    private long total;
    private boolean hasNext;
    private List<T> items;

    public static <T> PageResponse<T> of(long pageNo, long pageSize, long total, List<T> items) {
        long pages = pageSize == 0 ? 0 : (long) Math.ceil((double) total / (double) pageSize);
        return new PageResponse<>(pageNo, pageSize, total, pageNo < pages, items);
    }

    public static <T> PageResponse<T> empty(long pageNo, long pageSize) {
        return new PageResponse<>(pageNo, pageSize, 0, false, Collections.emptyList());
    }
}
