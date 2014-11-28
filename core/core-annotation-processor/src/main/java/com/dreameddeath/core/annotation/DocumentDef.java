package com.dreameddeath.core.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Created by CEAJ8230 on 26/11/2014.
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface DocumentDef {
    String domain();
    String name() default "";
    String version() default "1.0.0";
}
