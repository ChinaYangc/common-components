package com.fansz.orm.dao.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 指示一个方法需要修改数据，而不是查询
 */
@Target({ ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface NamedExec {
    /**
     * 执行的增删改语句ID
     */
    String execId();

    /**
     * 参数
     */
    String[] parameters() default {};
}
