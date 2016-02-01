package com.fansz.event.model;

import com.fansz.pub.constant.InformationSource;
import com.fansz.pub.constant.PostType;

import java.io.Serializable;
import java.util.Date;

/**
 * POST发布事件
 */
public class PublishPostEvent extends AbstractStatistic implements Serializable {

    private static final long serialVersionUID = -4916842445941233425L;

    private Long postId;

    private String postTitle;

    private String postContent;

    private Date postTime;

    private PostType postType;

    private InformationSource source;

    public PublishPostEvent() {
        this.source = InformationSource.FANDOM;
        this.postType = PostType.POST;
    }

    public PublishPostEvent(Long postId, String memberSn) {
        this.postId = postId;
        this.setPostCreator(memberSn);
        this.postTime = new Date();
        this.source = InformationSource.FANDOM;
        this.postType = PostType.POST;
    }

    public PublishPostEvent(Long postId, String memberSn, Date postTime, String postTitle, String postContent, PostType postType) {
        this.postId = postId;
        this.setPostCreator(memberSn);
        this.postTime = postTime;
        this.postTitle = postTitle;
        this.postContent = postContent;
        this.postType = postType;
        this.source = InformationSource.FANDOM;
    }

    public PublishPostEvent(Long postId, String memberSn, Date postTime, InformationSource source) {
        this.postId = postId;
        this.setPostCreator(memberSn);
        this.postTime = postTime;
        this.source = source;
    }

    public Long getPostId() {
        return postId;
    }

    public void setPostId(Long postId) {
        this.postId = postId;
    }

    public InformationSource getSource() {
        return source;
    }

    public void setSource(InformationSource source) {
        this.source = source;
    }

    public String getPostTitle() {
        return postTitle;
    }

    public void setPostTitle(String postTitle) {
        this.postTitle = postTitle;
    }

    public String getPostContent() {
        return postContent;
    }

    public void setPostContent(String postContent) {
        this.postContent = postContent;
    }

    public Date getPostTime() {
        return postTime;
    }

    public void setPostTime(Date postTime) {
        this.postTime = postTime;
    }

    public PostType getPostType() {
        return postType;
    }

    public void setPostType(PostType postType) {
        this.postType = postType;
    }
}
