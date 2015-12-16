package com.fansz.orm.domain;

import java.util.Date;

/**
 * 创建时间
 */
public interface ICreateTimeRecordable {
    /**
     * 设定创建时间
     *
     * @param createTime 创建时间
     */
    void setCreateTime(Date createTime);

    /**
     * 取得创建时间
     *
     * @return 创建时间
     */
    Date getCreateTime();
}
