package com.fansz.event.model;

import java.io.Serializable;

/**
 * Created by allan on 16/2/1.
 * 创建fandom事件
 */
public class AddFandomEvent implements Serializable {

    private static final long serialVersionUID = -8963300249522108322L;

    private String memberSn;

    public String getMemberSn() {
        return memberSn;
    }

    public void setMemberSn(String memberSn) {
        this.memberSn = memberSn;
    }
}
