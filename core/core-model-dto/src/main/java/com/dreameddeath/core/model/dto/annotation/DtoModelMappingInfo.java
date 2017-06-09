package com.dreameddeath.core.model.dto.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Created by CEAJ8230 on 03/06/2017.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface DtoModelMappingInfo {
    String entityModelId();
    String entityClassName();
    DtoInOutMode mode();
    String type();
    String version();
}
