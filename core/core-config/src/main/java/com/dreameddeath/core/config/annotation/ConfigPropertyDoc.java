package com.dreameddeath.core.config.annotation;

/**
 * Created by CEAJ8230 on 21/05/2015.
 */
public @interface ConfigPropertyDoc {
    String name();
    String descr();
    String defaultValue() default "";
    String[] examples() default {};
}
