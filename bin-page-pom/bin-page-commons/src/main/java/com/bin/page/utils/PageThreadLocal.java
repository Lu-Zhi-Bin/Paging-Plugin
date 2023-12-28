package com.bin.page.utils;

import com.bin.page.entity.PagingInformation;

public class PageThreadLocal {
    private final static ThreadLocal<PagingInformation> PAGING_INFORMATION_THREAD_LOCAL = new ThreadLocal<>();

    /**
     * 获取分页信息
     *
     * @return 线程值
     */
    public static PagingInformation getPagingInformation() {
        return PAGING_INFORMATION_THREAD_LOCAL.get();
    }


    /**
     * 缓存分页参数信息（设置默认开启）
     *
     * @param pageNum  页数
     * @param pageSize 每页条数
     */
    public static void setPagingInformation(Integer pageNum, Integer pageSize) {
        PagingInformation page = new PagingInformation();
        page.setPageNum(pageNum);
        page.setPageSize(pageSize);
        page.setEnable(true);
        PageThreadLocal.PAGING_INFORMATION_THREAD_LOCAL.set(page);
    }

    /**
     * 清空线程分页缓存
     */
    public static void clearPage() {
        PageThreadLocal.PAGING_INFORMATION_THREAD_LOCAL.set(null);
    }
}
