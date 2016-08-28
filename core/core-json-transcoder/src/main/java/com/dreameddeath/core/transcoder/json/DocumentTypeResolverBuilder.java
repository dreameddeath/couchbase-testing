/*
 * Copyright Christophe Jeunesse
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.dreameddeath.core.transcoder.json;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.DeserializationConfig;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.annotation.NoClass;
import com.fasterxml.jackson.databind.cfg.MapperConfig;
import com.fasterxml.jackson.databind.jsontype.NamedType;
import com.fasterxml.jackson.databind.jsontype.TypeDeserializer;
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
            while(currClazz.getAnnotation(JsonTypeInfo.class)==null) {
                currClazz = currClazz.getSuperclass();
                if(currClazz==Object.class) break;
            }

            return new CustomMinimalClassNameIdResolver(config.constructType(currClazz), config.getTypeFactory());
        }
        return super.idResolver(config,baseType,subtypes,forSer,forDeser);

    }

    @Override
    public TypeDeserializer buildTypeDeserializer(DeserializationConfig config,
                                                  JavaType baseType, Collection<NamedType> subtypes)
    {

        if(     (_idType!= JsonTypeInfo.Id.NONE) && (_includeAs== JsonTypeInfo.As.PROPERTY)
                && (_customIdResolver!=null) && (_customIdResolver instanceof CouchbaseDocumentTypeIdResolver)
                )
        {
            /*Copy paste of parent class*/
            JavaType defaultImpl;

            if (_defaultImpl == null) {
                defaultImpl = null;
            } else {
                // 20-Mar-2016, tatu: It is important to do specialization go through
                //   TypeFactory to ensure proper resolution; with 2.7 and before, direct
                //   call to JavaType was used, but that can not work reliably with 2.7
                // 20-Mar-2016, tatu: Can finally add a check for type compatibility BUT
                //   if so, need to add explicit checks for marker types. Not ideal, but
                //   seems like a reasonable compromise.
                if ((_defaultImpl == Void.class)
                        || (_defaultImpl == NoClass.class)) {
                    defaultImpl = config.getTypeFactory().constructType(_defaultImpl);
                } else {
                    defaultImpl = config.getTypeFactory()
                            .constructSpecializedType(baseType, _defaultImpl);
                }
            }



            TypeIdResolver idRes = idResolver(config, baseType, subtypes, false, true);
            return new CustomAsPropertyDeserializer(baseType, idRes,
                    _typeProperty, _typeIdVisible, defaultImpl, _includeAs);
        }

        return super.buildTypeDeserializer(config, baseType, subtypes);
    }
}
