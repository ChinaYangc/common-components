package com.fansz.event.model;

/**
 * Created by allan on 16/2/1.
 */
public abstract class AbstractStatistic {
    /**
     * 发帖人,为了统计用途
     */
    private String postCreator;

    /**
     * fandom ID,为了统计用途
     */
    private Long fandomId;

    public String getPostCreator() {
        return postCreator;
    }

    public void setPostCreator(String postCreator) {
        this.postCreator = postCreator;
    }

    public Long getFandomId() {
        return fandomId;
    }

    public void setFandomId(Long fandomId) {
        this.fandomId = fandomId;
    }
}
