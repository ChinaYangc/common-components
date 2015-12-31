package com.fansz.common.provider.model;

import java.io.Serializable;
import java.util.List;

/**
 * Created by allan on 15/11/30.
 */
public class CommonPagedResult<T> implements Serializable {

    private static final long serialVersionUID = 4199421899377849539L;

    private String status;

    private String message;

    private PagedResult result;

    public CommonPagedResult() {
        result = new PagedResult();
    }

    public CommonPagedResult(String status, String message) {
        this.status = status;
        this.message = message;
        result = new PagedResult();
    }

    public CommonPagedResult(String status, String message, PageParam pager,List<T> data) {
        this.status = status;
        this.message = message;
        result = new PagedResult();
        result.setPager(pager);
        result.setData(data);
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public PagedResult getResult() {
        return result;
    }

    public void setResult(PagedResult result) {
        this.result = result;
    }



}
