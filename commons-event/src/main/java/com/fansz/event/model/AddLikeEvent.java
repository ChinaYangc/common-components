package com.fansz.event.model;

import com.fansz.pub.constant.InformationSource;

import java.io.Serializable;

/**
 * 点赞事件
 */
public class AddLikeEvent extends AbstractStatistic implements Serializable {

    /**
     * 来源
     */
    private InformationSource source=InformationSource.NEWSFEEDS;


    private Long postId;

    /**
     * 点赞ID
     */
    private Long likeId;

    /**
     * 点赞人
     */
    private String memberSn;

    public AddLikeEvent(){
        this.source=InformationSource.NEWSFEEDS;
    }

    public AddLikeEvent(Long likeId,Long postId, String memberSn) {
        this.postId = postId;
        this.likeId = likeId;
        this.memberSn = memberSn;
        this.source=InformationSource.NEWSFEEDS;
    }

    public InformationSource getSource() {
        return source;
    }

    public void setSource(InformationSource source) {
        this.source = source;
    }

    public Long getPostId() {
        return postId;
    }

    public void setPostId(Long postId) {
        this.postId = postId;
    }

    public Long getLikeId() {
        return likeId;
    }

    public void setLikeId(Long likeId) {
        this.likeId = likeId;
    }

    public String getMemberSn() {
        return memberSn;
    }

    public void setMemberSn(String memberSn) {
        this.memberSn = memberSn;
    }
}
