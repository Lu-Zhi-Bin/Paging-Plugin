package com.bin.page.result;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.io.Serializable;
import java.util.Map;

/**
 * 接口返回统一对象（为null的值不返回）
 * 需要返回对象去继承这个，才能一起返回分页信息
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PageResult implements Serializable {
    /**
     * 分页对象
     */
    private Map<String, Integer> page;

    /**
     * 无参构造方法
     */
    public PageResult() {
    }

    public Map<String, Integer> getPage() {
        return page;
    }

    public void setPage(Map<String, Integer> page) {
        this.page = page;
    }
}
