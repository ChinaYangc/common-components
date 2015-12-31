package com.fansz.event.model;

import java.io.Serializable;

/**
 * Created by allan on 15/12/21.
 */
public class SmsEvent implements Serializable {
    private static final long serialVersionUID = 8005578263274440012L;

    private String content;

    private String mobile;

    public SmsEvent() {
    }

    public SmsEvent(String content, String mobile) {
        this.content = content;
        this.mobile = mobile;
    }

    public String getMobile() {
        return mobile;
    }

    public void setMobile(String mobile) {
        this.mobile = mobile;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}
