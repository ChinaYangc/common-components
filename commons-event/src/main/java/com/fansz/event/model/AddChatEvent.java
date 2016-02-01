package com.fansz.event.model;

import java.io.Serializable;

/**
 * Created by allan on 16/2/1.
 * 创建新的聊天会话窗口事件
 */
public class AddChatEvent implements Serializable {

    private static final long serialVersionUID = 8376633124311687810L;

    private String memberSn;

    public String getMemberSn() {
        return memberSn;
    }

    public void setMemberSn(String memberSn) {
        this.memberSn = memberSn;
    }
}
