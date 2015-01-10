package com.dreameddeath.core.annotation.dao;

import java.lang.annotation.*;

/**
 * Created by CEAJ8230 on 08/01/2015.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Repeatable(Counters.class)
public @interface Counter {
    String name();
    String dbName();
    boolean isKeyGen() default false;
    int defaultValue() default 1;
    long modulus() default 0;
}
