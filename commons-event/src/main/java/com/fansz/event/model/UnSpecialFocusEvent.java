package com.fansz.event.model;


import java.io.Serializable;

/**
 * Created by allan on 15/12/21.
 */
public class UnSpecialFocusEvent implements Serializable {

    private String currentSn;

    private String specialMemberSn;

    private Long specialFandomId;

    public UnSpecialFocusEvent(){

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
}
