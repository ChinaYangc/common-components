package com.fansz.common.provider.model;

/**
 *
 */
public interface AccessTokenAware {
    /*
      获取请求传递的accessToken
     */
    String getAccessToken();

    /**
     * 获取当前登陆用户的sn
     *
     * @return
     */
    String getCurrentSn();

    /**
     * 设置当前登陆用户的sn
     */
    void setCurrentSn(String currentSn);

}
