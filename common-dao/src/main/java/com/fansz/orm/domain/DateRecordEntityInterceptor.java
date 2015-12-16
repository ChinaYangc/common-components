package com.fansz.orm.domain;

import java.io.Serializable;

import org.hibernate.EmptyInterceptor;
import org.hibernate.type.Type;

import com.fansz.pub.utils.DateTools;

/**
 * 自动记录创建时间和更新时间的EntityListener
 */
public class DateRecordEntityInterceptor extends EmptyInterceptor {
    private static final long serialVersionUID = 9207627643785486171L;

    /**
     * 当持久化对象更新时，在更新前就会执行这个函数，用于自动更新修改日期字段
     *
     * @param event 事件对象
     */
    @Override
    public boolean onFlushDirty(Object entity, Serializable id, Object[] currentState, Object[] previousState,
            String[] propertyNames, Type[] types) {
        if (entity instanceof IUpdateTimeRecordable) {
            for (int index = propertyNames.length - 1; index >= 0; index--) {
                // 找到名为"修改日期"的属性
                if ("updateTime".equals(propertyNames[index])) {
                    // 使用拦截器将对象的"修改日期"属性赋上值
                    currentState[index] = DateTools.getSysDate();
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * 当保存一个entity对象时，在保存之前会执行这个函数，用于自动添加创建日期
     *
     * @param o 被保存的对象
     */
    @Override
    public boolean onSave(Object entity, Serializable id, Object[] state, String[] propertyNames, Type[] types) {
        if (entity instanceof ICreateTimeRecordable) {
            for (int index = propertyNames.length - 1; index >= 0; index--) {
                // 找到名为"添加日期"的属性
                if ("createTime".equals(propertyNames[index])) {
                    // 使用拦截器将对象的"添加日期"属性赋上值
                    if (state[index] == null) {
                        state[index] = DateTools.getSysDate();
                        return true;
                    }
                    break;
                }
            }
        }
        return false;
    }
}
