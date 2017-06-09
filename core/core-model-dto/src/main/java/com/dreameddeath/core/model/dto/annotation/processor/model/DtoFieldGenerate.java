package com.dreameddeath.core.model.dto.annotation.processor.model;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Created by CEAJ8230 on 02/06/2017.
 */
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface DtoFieldGenerate {
    DtoFieldGenerateType[] buildForTypes() default {};
}
