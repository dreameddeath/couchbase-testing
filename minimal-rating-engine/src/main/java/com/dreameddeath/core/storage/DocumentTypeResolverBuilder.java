package com.dreameddeath.core.storage;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.cfg.MapperConfig;
import com.fasterxml.jackson.databind.jsontype.NamedType;
import com.fasterxml.jackson.databind.jsontype.TypeIdResolver;
import com.fasterxml.jackson.databind.jsontype.impl.StdTypeResolverBuilder;

import java.util.Collection;

/**
 * Created by Christophe Jeunesse on 03/08/2014.
 */
public class DocumentTypeResolverBuilder extends StdTypeResolverBuilder {
    /**
     * Helper method that will either return configured custom
     * type id resolver, or construct a standard resolver
     * given configuration.
     */
    @Override
    protected TypeIdResolver idResolver(MapperConfig<?> config,
                                        JavaType baseType, Collection<NamedType> subtypes, boolean forSer, boolean forDeser)
    {
        if((_idType!=null)&&(_idType== JsonTypeInfo.Id.MINIMAL_CLASS)){
            Class<?> currClazz=baseType.getRawClass();
            JsonTypeInfo typeIdAnn;
            while(currClazz.getAnnotation(JsonTypeInfo.class)==null) {
                currClazz = currClazz.getSuperclass();
                if(currClazz==Object.class) break;
            }

            return new CustomMinimalClassNameIdResolver(config.constructType(currClazz), config.getTypeFactory());
        }
        return super.idResolver(config,baseType,subtypes,forSer,forDeser);

    }
}
