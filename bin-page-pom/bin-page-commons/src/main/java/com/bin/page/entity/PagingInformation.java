package com.bin.page.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.io.Serializable;

/**
 * 分页信息
 */
public class PagingInformation implements Serializable {
    /**
     * 插件启用状态
     * 默认状态：启用
     */
    @JsonIgnore
    private boolean enable = true;

    /**
     * 页码
     */
    private Integer pageNum = 1;

    /**
     * 每页的条数
     */
    private Integer pageSize = 10;

    /**
     * 总条数
     */
    private Integer count;

    /**
     * 总页数
     */
    private Integer total;


    public PagingInformation() {
    }

    public PagingInformation(Integer pageNum, Integer pageSize) {
        this.pageNum = pageNum;
        this.pageSize = pageSize;
    }

    public boolean getEnable() {
        return enable;
    }

    public void setEnable(boolean enable) {
        this.enable = enable;
    }

    public Integer getCount() {
        return count;
    }

    public void setCount(Integer count) {
        this.count = count;
    }

    public Integer getTotal() {
        return total;
    }

    public void setTotal(Integer total) {
        this.total = total;
    }

    public Integer getPageNum() {
        return pageNum;
    }

    public void setPageNum(Integer pageNum) {
        this.pageNum = pageNum;
    }

    public Integer getPageSize() {
        return pageSize;
    }

    public void setPageSize(Integer pageSize) {
        this.pageSize = pageSize;
    }
}
