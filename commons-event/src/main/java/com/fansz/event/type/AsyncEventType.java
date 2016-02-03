package com.fansz.event.type;

/**
 * Created by allan on 15/12/21.
 */
public enum AsyncEventType {

    PUBLISH_POST("01", "POST"),//发布帖子
    SEND_SMS("02", "SMS"),
    ADD_COMMENT("03", "COMMENT"),//添加评论
    ADD_LIKE("04", "LIKE"),//点赞
    FORWARD_POST("05", "FORWARD"),//转发帖子
    ADD_FRIEND("06", "ADD_FRIEND"),//添加好友
    ADD_FANDOM("07", "ADD_FANDOM"),//创建fandom
    ADD_CHAT("08", "ADD_CHAT"),//创建新的聊天会话
    USER("09", "USER");//用户事件(用户注册,修改)

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
