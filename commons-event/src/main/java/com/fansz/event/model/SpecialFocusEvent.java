package com.fansz.event.model;


import java.io.Serializable;

/**
 * Created by allan on 15/12/21.
 */
public class SpecialFocusEvent implements Serializable {
    private static final long serialVersionUID = -3131587799420844086L;

    private String currentSn;

    private String specialMemberSn;

    private Long specialFandomId;

    private Long postingTag;


    public SpecialFocusEvent() {

    }

    public SpecialFocusEvent(String currentSn, String specialMemberSn, Long specialFandomId, Long postingTag) {
        this.currentSn = currentSn;
        this.specialMemberSn = specialMemberSn;
        this.specialFandomId = specialFandomId;
        this.postingTag = postingTag;
    }

    public String getCurrentSn() {
        return currentSn;
    }

    public void setCurrentSn(String currentSn) {
        this.currentSn = currentSn;
    }

    public String getSpecialMemberSn() {
        return specialMemberSn;
    }

    public void setSpecialMemberSn(String specialMemberSn) {
        this.specialMemberSn = specialMemberSn;
    }

    public Long getSpecialFandomId() {
        return specialFandomId;
    }

    public void setSpecialFandomId(Long specialFandomId) {
        this.specialFandomId = specialFandomId;
    }

    public Long getPostingTag() {
        return postingTag;
    }

    public void setPostingTag(Long postingTag) {
        this.postingTag = postingTag;
    }
}
