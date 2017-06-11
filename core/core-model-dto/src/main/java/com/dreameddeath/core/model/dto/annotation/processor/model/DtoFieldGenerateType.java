package com.dreameddeath.core.model.dto.annotation.processor.model;

import com.dreameddeath.core.model.dto.annotation.processor.model.AbstractDtoModelGenerator.FieldGenMode;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Created by CEAJ8230 on 02/06/2017.
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface DtoFieldGenerateType {
    String version() default DtoGenerateType.DEFAULT_VERSION;
    String type() default DtoGenerateType.DEFAULT_TYPE_NAME;
    FieldGenMode inputFieldMode() default FieldGenMode.SIMPLE;
    FieldGenMode outputFieldMode() default FieldGenMode.SIMPLE;
    FieldGenMode unwrapDefaultFieldMode() default FieldGenMode.INHERIT;
    String name() default "";
}
