package com.fansz.pub.constant;

/**
 * Created by allan on 15/12/26.
 */
public enum InformationSource {
    FANDOM("T"), NEWSFEEDS("P");

    private String code;

    private InformationSource(String code) {
        this.code = code;
    }

    public String getCode() {
        return this.code;
    }
}
