package com.dreameddeath.core.model.dto.annotation;

import java.lang.annotation.*;

/**
 * Created by CEAJ8230 on 30/05/2017.
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@Repeatable(DtoFieldMappingInfos.class)
public @interface DtoFieldMappingInfo {
    enum MappingRuleType{
        COPY,
        SIMPLE_MAP,
        MVEL,
        STATIC_METHOD
    }
    MappingRuleType mappingType() default MappingRuleType.COPY;
    String ruleValue() default "";
    DtoInOutMode mode() default DtoInOutMode.BOTH;
}
