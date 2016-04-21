package com.dreameddeath.core.model.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Created by Christophe Jeunesse on 20/04/2016.
 */
@Target({ElementType.PACKAGE})
@Retention(RetentionPolicy.RUNTIME)
public @interface DocumentPackageDefault {
    String domain();
    String version();
}
