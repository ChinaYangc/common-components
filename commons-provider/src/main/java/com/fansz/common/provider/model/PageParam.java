package com.fansz.common.provider.model;

import com.alibaba.fastjson.annotation.JSONField;

import java.io.Serializable;

/**
 * 分页参数
 */
public class PageParam implements Serializable {

    private static final long serialVersionUID = 2149367388124720438L;

    @JSONField(name="page_size")
    private Integer pageSize = 10;

    @JSONField(name="page_num")
    private Integer pageNum = 1;

    @JSONField(name="total_num")
    private Long totalNum;

    public Integer getPageSize() {
        return pageSize;
    }

    public void setPageSize(Integer pageSize) {
        this.pageSize = pageSize;
    }

    public Integer getPageNum() {
        return pageNum;
    }

    public void setPageNum(Integer pageNum) {
        this.pageNum = pageNum;
    }

    public Long getTotalNum() {
        return totalNum;
    }

    public void setTotalNum(Long totalNum) {
        this.totalNum = totalNum;
    }
}
