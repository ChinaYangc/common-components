package com.fansz.event.model;

import com.fansz.pub.constant.InformationSource;

import java.io.Serializable;

/**
 * 添加评论事件
 */
public class AddCommentEvent implements Serializable {
    /**
     * 来源
     */
    private InformationSource source = InformationSource.NEWSFEEDS;
    /**
     * 评论ID
     */
    private Long commentId;

    /**
     * post ID
     */

    private Long postId;

    /**
     * 评论父ID
     */
    private Long commentParentId;

    /**
     * 评论人
     */
    private String memberSn;
    /**
     * 评论内容
     */
    private String commentContent;

    public AddCommentEvent() {
        this.source = InformationSource.NEWSFEEDS;
    }

    public AddCommentEvent(Long commentId, String memberSn, String commentContent) {
        this(commentId, null, null, memberSn, commentContent);
    }

    public AddCommentEvent(Long commentId, Long postId, String memberSn, String commentContent) {
        this(commentId, postId, null, memberSn, commentContent);
    }

    public AddCommentEvent(Long commentId, Long postId, Long commentParentId, String memberSn, String commentContent) {
        this.commentId = commentId;
        this.postId = postId;
        this.commentParentId = commentParentId;
        this.memberSn = memberSn;
        this.commentContent = commentContent;
    }


    public InformationSource getSource() {
        return source;
    }

    public void setSource(InformationSource source) {
        this.source = source;
    }

    public Long getCommentId() {
        return commentId;
    }

    public void setCommentId(Long commentId) {
        this.commentId = commentId;
    }

    public Long getPostId() {
        return postId;
    }

    public void setPostId(Long postId) {
        this.postId = postId;
    }

    public Long getCommentParentId() {
        return commentParentId;
    }

    public void setCommentParentId(Long commentParentId) {
        this.commentParentId = commentParentId;
    }

    public String getMemberSn() {
        return memberSn;
    }

    public void setMemberSn(String memberSn) {
        this.memberSn = memberSn;
    }

    public String getCommentContent() {
        return commentContent;
    }

    public void setCommentContent(String commentContent) {
        this.commentContent = commentContent;
    }
}
