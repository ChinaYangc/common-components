package com.fansz.pub.constant;

/**
 * Created by allan on 16/1/14.
 */
public enum PostType {

    POST("P"),VOTE_POST("V");

    private String code;

    private PostType(String code) {
        this.code = code;
    }

    public String getCode() {
        return this.code;
    }


    public static PostType getTypeByCode(String code) {
        PostType[] codes = values();
        int len = codes.length;

        for (int i = 0; i < len; ++i) {
            PostType postType = codes[i];
            if (postType.getCode().equals(code)) {
                return postType;
            }
        }

        return null;
    }
}
