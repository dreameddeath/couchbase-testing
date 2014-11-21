package com.dreameddeath.core.annotation;

import com.dreameddeath.core.validation.Validator;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Created by ceaj8230 on 16/08/2014.
 */

@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidationConstraint {
    Class<? extends Validator> validationClass();
}
