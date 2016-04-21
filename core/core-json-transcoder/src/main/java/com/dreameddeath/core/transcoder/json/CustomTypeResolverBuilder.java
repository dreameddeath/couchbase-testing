package com.dreameddeath.core.transcoder.json;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.DeserializationConfig;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.jsontype.NamedType;
import com.fasterxml.jackson.databind.jsontype.TypeDeserializer;

import java.util.Collection;

/**
 * Created by Christophe Jeunesse on 20/04/2016.
 */
public class CustomTypeResolverBuilder extends ObjectMapper.DefaultTypeResolverBuilder {
    public CustomTypeResolverBuilder(ObjectMapper.DefaultTyping t) {
        super(t);
    }

    @Override
    public TypeDeserializer buildTypeDeserializer(DeserializationConfig config,
                                                  JavaType baseType, Collection<NamedType> subtypes)
    {
        if(useForType(baseType)){
            if(     (_idType!= JsonTypeInfo.Id.NONE) && (_includeAs== JsonTypeInfo.As.PROPERTY)
                    && (_customIdResolver!=null) && (_customIdResolver instanceof CouchbaseDocumentTypeIdResolver)
                    )
            {
                return null;
            }
        }
        return super.buildTypeDeserializer(config, baseType, subtypes);
    }

}
