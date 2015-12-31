package com.fansz.common.provider.annotation;

import java.lang.annotation.*;

/**
 * Created by allan on 15/12/14.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface DubboxService {
    String value();
}
