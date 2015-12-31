package com.fansz.common.provider.model;

import java.io.Serializable;

/**
 * Created by allan on 15/11/22.
 */
public class CommonResult<T> implements Serializable {

    private static final long serialVersionUID = 4199421899377849539L;

    private String status;

    private String message;


    private T result;

    public CommonResult() {

    }

    public CommonResult(String status, String message) {
        this.status = status;
        this.message = message;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public T getResult() {
        return result;
    }

    public void setResult(T result) {
        this.result = result;
    }


    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
