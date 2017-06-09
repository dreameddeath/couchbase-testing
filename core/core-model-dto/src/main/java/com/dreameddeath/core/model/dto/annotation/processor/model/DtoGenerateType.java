package com.dreameddeath.core.model.dto.annotation.processor.model;

import com.dreameddeath.core.model.dto.annotation.processor.model.AbstractDtoModelGenerator.FieldGenMode;
import com.dreameddeath.core.model.dto.annotation.processor.model.AbstractDtoModelGenerator.SuperClassGenMode;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Created by CEAJ8230 on 30/05/2017.
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface DtoGenerateType {
    String DEFAULT_VERSION="1.0";
    String DEFAULT_TYPE_NAME="";
    SuperClassGenMode DEFAULT_SUPERCLASS_GENMODE = SuperClassGenMode.AUTO;
    FieldGenMode DEFAULT_FIELD_MODE=FieldGenMode.SIMPLE;

    String version() default DEFAULT_VERSION;
    String name() default DEFAULT_TYPE_NAME;
    FieldGenMode defaultInputFieldMode() default FieldGenMode.SIMPLE;
    FieldGenMode defaultOutputFieldMode() default FieldGenMode.SIMPLE;
    SuperClassGenMode superClassGenMode() default SuperClassGenMode.AUTO;
}
