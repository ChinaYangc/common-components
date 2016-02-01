package com.fansz.event.model;

import java.io.Serializable;

/**
 * Created by allan on 16/2/1.
 * 添加好友事件
 */
public class AddFriendEvent implements Serializable {

    private static final long serialVersionUID = 6981414417737757876L;

    private String myMemberSn;

    private String friendMemberSn;

    public String getMyMemberSn() {
        return myMemberSn;
    }

    public void setMyMemberSn(String myMemberSn) {
        this.myMemberSn = myMemberSn;
    }

    public String getFriendMemberSn() {
        return friendMemberSn;
    }

    public void setFriendMemberSn(String friendMemberSn) {
        this.friendMemberSn = friendMemberSn;
    }
}
