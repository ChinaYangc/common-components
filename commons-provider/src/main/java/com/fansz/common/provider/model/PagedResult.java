package com.fansz.common.provider.model;

import java.io.Serializable;
import java.util.List;

/**
 * Created by allan on 15/12/25.
 */
public class PagedResult<T> implements Serializable {
    private static final long serialVersionUID = -4760300387808556112L;

    private List<T> data;

    private PageParam pager;

    public PagedResult() {

    }

    public List<T> getData() {
        return data;
    }

    public void setData(List<T> data) {
        this.data = data;
    }

    public PageParam getPager() {
        return pager;
    }

    public void setPager(PageParam pager) {
        this.pager = pager;
    }
}
