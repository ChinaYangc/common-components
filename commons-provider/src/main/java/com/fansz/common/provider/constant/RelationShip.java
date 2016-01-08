package com.fansz.common.provider.constant;

/**
 * Created by allan on 15/11/30.
 */
public enum RelationShip {

    TO_ADD("00"), BE_ADDED("01"), FRIEND("10"), SPECIAL_FRIEND("11");

    private String code;

    RelationShip(String code) {
        this.code = code;
    }

    public String getCode() {
        return this.code;
    }
}