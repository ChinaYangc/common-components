package com.fansz.event.model;


import java.io.Serializable;

/**
 * Created by allan on 15/12/21.
 */
public class AsyncEventObject<T> implements Serializable {

    private static final long serialVersionUID = -3131587799420844086L;

    private T param;

    public AsyncEventObject() {

    }


    public AsyncEventObject(T param) {
        this.param = param;
    }

    public T getParam() {
        return param;
    }

    public void setParam(T param) {
        this.param = param;
    }
}
