package com.fansz.event.type;

/**
 * Created by allan on 15/12/21.
 */
public enum AsyncEventType {

    PUBLISH_POST("01", "POST"), SEND_SMS("02", "SMS"), SPECIAL_FOCUS("03", "FOCUS"), UN_SPECIAL_FOCUS("04", "FOCUS");

    private String code;

    private String name;

    AsyncEventType(String code, String name) {
        this.code = code;
        this.name = name;
    }

    public String getCode() {
        return this.code;
    }

    public String getName() {
        return name;
    }

    public static AsyncEventType getTypeByCode(String code) {
        for (AsyncEventType eventType : AsyncEventType.values()) {
            if (eventType.getCode().equals(code)) {
                return eventType;
            }
        }
        return null;
    }
}
