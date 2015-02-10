package com.dreameddeath.core.annotation.dao;

import java.lang.annotation.*;

/**
 * Created by ceaj8230 on 10/02/2015.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Repeatable(Views.class)
public @interface View {
    String name();
    String domain() default "";
    String content() default "";
    String contentFilename() default "";
    ViewKeyDef keyDef();
    ViewValueDef valueDef();
}
