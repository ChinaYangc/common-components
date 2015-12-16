package com.fansz.orm.dao.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 指示一个方法是通过findByNamedQuery来实现的
 */
@Target({ ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface NamedQuery {
    /**
     * 查询ID
     */
    String queryId();

    /**
     * 查询参数
     */
    String[] parameters() default {};
}
