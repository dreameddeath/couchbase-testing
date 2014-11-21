package com.dreameddeath.core.transcoder.json;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.jsontype.impl.MinimalClassNameIdResolver;
import com.fasterxml.jackson.databind.type.TypeFactory;

/**
 * Created by Christophe Jeunesse on 03/08/2014.
 */
public class CustomMinimalClassNameIdResolver extends MinimalClassNameIdResolver{
    protected CustomMinimalClassNameIdResolver(JavaType baseType, TypeFactory typeFactory){
        super(baseType,typeFactory);
    }
}
