package com.fansz.orm.domain;

import java.util.Date;

/**
 * 修改时间
 */
public interface IUpdateTimeRecordable {
    /**
     * 设置修改时间
     * 
     * @param updateTime 修改时间
     */
    void setUpdateTime(Date updateTime);

    /**
     * 取得修改时间
     * 
     * @return 修改时间
     */
    Date getUpdateTime();
}
