package com.fansz.event.model;

import com.fansz.pub.constant.InformationSource;

import java.io.Serializable;
import java.util.Date;

/**
 * POST发布事件
 */
public class PublishPostEvent implements Serializable {

    private static final long serialVersionUID = -4916842445941233425L;

    private Long postId;

    private String memberSn;

    private Date postTime;

    private InformationSource source;

    public PublishPostEvent() {
        this.source = InformationSource.FANDOM;
    }

    public PublishPostEvent(Long postId, String memberSn) {
        this.postId = postId;
        this.memberSn = memberSn;
        this.postTime = new Date();
        this.source = InformationSource.FANDOM;
    }

    public PublishPostEvent(Long postId, String memberSn, Date postTime) {
        this.postId = postId;
        this.memberSn = memberSn;
        this.postTime = postTime;
        this.source = InformationSource.FANDOM;
    }

    public PublishPostEvent(Long postId, String memberSn, Date postTime, InformationSource source) {
        this.postId = postId;
        this.memberSn = memberSn;
        this.postTime=postTime;
        this.source = source;
    }

    public Long getPostId() {
        return postId;
    }

    public void setPostId(Long postId) {
        this.postId = postId;
    }

    public String getMemberSn() {
        return memberSn;
    }

    public void setMemberSn(String memberSn) {
        this.memberSn = memberSn;
    }

    public InformationSource getSource() {
        return source;
    }

    public void setSource(InformationSource source) {
        this.source = source;
    }
}
