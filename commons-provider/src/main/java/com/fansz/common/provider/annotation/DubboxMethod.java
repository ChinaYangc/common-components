package com.fansz.common.provider.annotation;

import java.lang.annotation.*;

/**
 * Created by allan on 15/12/26.
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface DubboxMethod {
    String value() default "execute";
}
