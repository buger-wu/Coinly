package com.coinly.common.domain;

import java.util.List;

/**
 * 分页查询统一响应封装。
 * 用于所有分页接口的返回，结构符合产品文档规范：
 */
public class PageResponse<T> {

    /** 当前页的数据列表 */
    private final List<T> records;

    /** 总记录数 */
    private final long total;

    /** 当前页码，从 1 开始 */
    private final int page;

    /** 每页大小 */
    private final int size;

    private PageResponse(List<T> records, long total, int page, int size) {
        this.records = records;
        this.total = total;
        this.page = page;
        this.size = size;
    }

    /**
     * 工厂方法：从分页查询结果构造响应。
     *
     * @param records 当前页数据
     * @param total   总记录数
     * @param page    当前页码
     * @param size    每页大小
     */
    public static <T> PageResponse<T> of(List<T> records, long total, int page, int size) {
        return new PageResponse<>(records, total, page, size);
    }

    public List<T> getRecords() {
        return records;
    }

    public long getTotal() {
        return total;
    }

    public int getPage() {
        return page;
    }

    public int getSize() {
        return size;
    }
}