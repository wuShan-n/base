package com.example.common.model;

import java.util.Collections;
import java.util.List;

/**
 * 分页结果
 */
public class PageResult<T> {
    private int pageNo;
    private int pageSize;
    private long total;
    private boolean hasNext;
    private List<T> items;

    public PageResult() {}

    public PageResult(int pageNo, int pageSize, long total, List<T> items) {
        this.pageNo = pageNo;
        this.pageSize = pageSize;
        this.total = total;
        this.items = (items == null ? Collections.emptyList() : items);
        this.hasNext = (long) pageNo * pageSize < total;
    }

    public static <T> PageResult<T> of(int pageNo, int pageSize, long total, List<T> items) {
        return new PageResult<>(pageNo, pageSize, total, items);
    }

    public int getPageNo() { return pageNo; }
    public void setPageNo(int pageNo) { this.pageNo = pageNo; }
    public int getPageSize() { return pageSize; }
    public void setPageSize(int pageSize) { this.pageSize = pageSize; }
    public long getTotal() { return total; }
    public void setTotal(long total) { this.total = total; }
    public boolean isHasNext() { return hasNext; }
    public void setHasNext(boolean hasNext) { this.hasNext = hasNext; }
    public List<T> getItems() { return items; }
    public void setItems(List<T> items) { this.items = items; }
}
