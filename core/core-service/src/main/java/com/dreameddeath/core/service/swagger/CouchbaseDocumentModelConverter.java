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

package com.dreameddeath.core.service.swagger;

import com.dreameddeath.compile.tools.annotation.processor.reflection.ParameterizedTypeInfo;
import com.dreameddeath.core.json.ObjectMapperFactory;
import com.dreameddeath.core.model.util.CouchbaseDocumentFieldReflection;
import com.dreameddeath.core.model.util.CouchbaseDocumentStructureReflection;
import com.dreameddeath.core.service.utils.ServiceObjectMapperConfigurator;
import com.fasterxml.jackson.databind.JavaType;
import io.swagger.converter.ModelConverter;
import io.swagger.converter.ModelConverterContext;
import io.swagger.jackson.AbstractModelConverter;
import io.swagger.jackson.TypeNameResolver;
import io.swagger.models.Model;
import io.swagger.models.ModelImpl;
import io.swagger.models.properties.ArrayProperty;
import io.swagger.models.properties.MapProperty;
import io.swagger.models.properties.Property;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.Iterator;

/**
 * Created by Christophe Jeunesse on 24/04/2015.
 */
public class CouchbaseDocumentModelConverter extends AbstractModelConverter {
    private static Logger LOG = LoggerFactory.getLogger(CouchbaseDocumentModelConverter.class);

    public CouchbaseDocumentModelConverter() {
        super(ObjectMapperFactory.BASE_INSTANCE.getMapper(ServiceObjectMapperConfigurator.SERVICE_MAPPER_CONFIGURATOR));
    }


    @Override
    public Model resolve(Type type, ModelConverterContext modelConverterContext, Iterator<ModelConverter> iterator) {
        Class rawClass = null;
        if(type instanceof JavaType){
            rawClass=((JavaType)type).getRawClass();
        }
        else if(type instanceof Class){
            rawClass = (Class)type;
        }
        if((rawClass!=null) && CouchbaseDocumentStructureReflection.isReflexible(rawClass)){
            CouchbaseDocumentStructureReflection eltReflexion = CouchbaseDocumentStructureReflection.getReflectionFromClassInfo(rawClass);
            ModelImpl model = new ModelImpl()
                    .name(TypeNameResolver.std.nameForType(_mapper.constructType(type)));
            for(CouchbaseDocumentFieldReflection field:eltReflexion.getFields()){
                String propName = field.getName();
                Property prop;
                if(field.isCollection()) {
                    prop = new ArrayProperty()
                            .items(resolveProperty(field.getCollectionElementTypeInfo(),field.getField().getAnnotations(),modelConverterContext,iterator));
                }
                else if(field.isMap()){
                    prop = new MapProperty()
                            .additionalProperties(resolveProperty(field.getMapValueTypeInfo(),field.getField().getAnnotations(),modelConverterContext,iterator));
                }
                else{
                    prop = resolveProperty(field.getEffectiveTypeInfo(),field.getField().getAnnotations(),modelConverterContext,iterator);
                }
                prop.setName(propName);
                model.addProperty(propName,prop);
                //model.addRequired(propName);
                //prop.setRequired(field.getField().getAnnotation(NotNull));
            }
            //Todo add description

            LOG.debug("founding class {}", type);

            return model;
        }
        return super.resolve(type, modelConverterContext, iterator);
    }

    protected Property resolveProperty(ParameterizedTypeInfo info,Annotation[] annotations,ModelConverterContext modelConverterContext, Iterator<ModelConverter> iterator){
        return modelConverterContext.resolveProperty(info.getType(),annotations);
    }
}
