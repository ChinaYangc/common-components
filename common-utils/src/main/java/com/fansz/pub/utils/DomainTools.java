package com.fansz.pub.utils;

import javax.persistence.Entity;

/**
 * Domain相关工具类
 */
public final class DomainTools {
    private DomainTools() {

    }

    /**
     * 指定类是不是JPA的Domain类
     *
     * @param targetClass 指定类
     * @param <T> 类型
     * @return 是不是JPA的Domain类
     */
    public static <T> boolean isEntity(Class<T> targetClass) {
        return targetClass.isAnnotationPresent(Entity.class);
    }
}
