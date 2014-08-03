package com.dreameddeath.common.storage;

import com.fasterxml.jackson.annotation.JsonTypeId;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.jsontype.impl.MinimalClassNameIdResolver;
import com.fasterxml.jackson.databind.type.TypeFactory;

/**
 * Created by CEAJ8230 on 03/08/2014.
 */
public class CustomMinimalClassNameIdResolver extends MinimalClassNameIdResolver{
    protected CustomMinimalClassNameIdResolver(JavaType baseType, TypeFactory typeFactory)
    {
        super(baseType,typeFactory);
    }
}
