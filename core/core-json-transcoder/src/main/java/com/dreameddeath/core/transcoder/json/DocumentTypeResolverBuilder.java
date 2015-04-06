/*
 * Copyright Christophe Jeunesse
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package com.dreameddeath.core.transcoder.json;

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
