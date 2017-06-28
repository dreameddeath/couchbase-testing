/*
 * 	Copyright Christophe Jeunesse
 *
 * 	Licensed under the Apache License, Version 2.0 (the "License");
 * 	you may not use this file except in compliance with the License.
 * 	You may obtain a copy of the License at
 *
 * 	http://www.apache.org/licenses/LICENSE-2.0
 *
 * 	Unless required by applicable law or agreed to in writing, software
 * 	distributed under the License is distributed on an "AS IS" BASIS,
 * 	WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * 	See the License for the specific language governing permissions and
 * 	limitations under the License.
 *
 */

package com.dreameddeath.core.model.dto.json;

import com.dreameddeath.compile.tools.annotation.processor.reflection.AbstractClassInfo;
import com.dreameddeath.compile.tools.annotation.processor.reflection.ClassInfo;
import com.dreameddeath.core.java.utils.StringUtils;
import com.dreameddeath.core.json.ObjectMapperFactory;
import com.dreameddeath.core.model.dto.annotation.DtoModelJsonTypeId;
import com.dreameddeath.core.model.dto.converter.DtoConverterManager;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.DatabindContext;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.annotation.JsonTypeIdResolver;
import com.fasterxml.jackson.databind.jsontype.impl.TypeIdResolverBase;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.google.common.base.Preconditions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by christophe jeunesse on 14/06/2017.
 */
public class DtoModelTypeIdResolver extends TypeIdResolverBase {
    private static final Logger LOG = LoggerFactory.getLogger(DtoModelTypeIdResolver.class);
    public static final String ROOT_PATH= DtoConverterManager.ROOT_PATH;
    public static final String TYPE_ID_PATH = "jsonTypeId";
    private static final ObjectMapper mapper = ObjectMapperFactory.BASE_INSTANCE.getMapper();

    public static String buildFileName(ClassInfo classInfo,DtoModelJsonTypeId modelJsonTypeId){
        return String.format("%s/%s/%s.json",
                ROOT_PATH,
                TYPE_ID_PATH,
                classInfo.getFullName()
        );
    }

    public static void buildTypeInfoFile(Writer writer,ClassInfo classInfo,DtoModelJsonTypeId modelJsonTypeId) throws IOException{
        String fullName = classInfo.getFullName();
        String rootFullName=null;
        ClassInfo currClassInfo = classInfo;
        do{
            JsonTypeIdResolver annot =currClassInfo.getAnnotation(JsonTypeIdResolver.class);
            if(annot!=null) {
                AbstractClassInfo typeIdResolverClass = ClassInfo.getClassInfoFromAnnot(annot, JsonTypeIdResolver::value);
                if (typeIdResolverClass != null) {
                    if (typeIdResolverClass.isInstanceOf(DtoModelTypeIdResolver.class)) {
                        rootFullName = currClassInfo.getFullName();
                        break;
                    }
                }
            }
            currClassInfo=currClassInfo.getSuperClass();
        }while(currClassInfo!=null);

        if(rootFullName!=null){
            String typeName = StringUtils.isNotEmpty(modelJsonTypeId.value())?modelJsonTypeId.value():classInfo.getSimpleName();
            mapper.writeValue(writer, new TypeIdInfo(fullName, typeName, rootFullName));
        }
    }

    public List<TypeIdInfo> getTypeInfoDef(){
        try {
            PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
            Resource[] resultResources= resolver.getResources("classpath*:" + ROOT_PATH + "/" + TYPE_ID_PATH + "/**.json");
            List<TypeIdInfo> result = new ArrayList<>(resultResources.length);
            for(Resource entityResource:resultResources){
                try {
                    result.add(mapper.readValue(entityResource.getInputStream(), TypeIdInfo.class));
                }
                catch(Throwable e){
                    LOG.error("Cannot read entity file <"+entityResource.getFile().getAbsolutePath()+">",e);
                    throw new RuntimeException("Cannot read entity file <"+entityResource.getFilename()+">",e);
                }
            }
            return result;
        }
        catch(Exception e) {
            throw new RuntimeException(e);
        }
    }



    private JavaType baseType;
    private Map<String,JavaType> mapType = new ConcurrentHashMap<>();
    private Map<String,Class> mapClass= new ConcurrentHashMap<>();

    public  DtoModelTypeIdResolver() {
        this(null, null);
    }

    public DtoModelTypeIdResolver(JavaType baseType, TypeFactory typeFactory) {
        super(baseType,typeFactory);
        this.init(baseType);
    }

    @Override
    public void init(JavaType bt) {
        if(bt!=null){
            this.baseType=bt;
            List<TypeIdInfo> typeIdInfos = getTypeInfoDef();
            String className = ClassInfo.getClassInfo(bt.getRawClass()).getFullName();
            for(TypeIdInfo info : typeIdInfos){
                if(className.equals(info.rootClassName)){
                    try {
                        mapClass.put(info.typeId, Thread.currentThread().getContextClassLoader().loadClass(info.className));
                    }
                    catch(ClassNotFoundException e){
                        LOG.error("Unknown class %s",info.className);
                        throw new RuntimeException(e);
                    }
                }
            }
        }
    }

    @Override
    public String idFromValue(Object value) {
        DtoModelJsonTypeId model = value.getClass().getAnnotation(DtoModelJsonTypeId.class);
        return model.value();
    }

    @Override
    public String idFromValueAndType(Object value, Class<?> suggestedType) {
        return idFromValue(value);
    }

    @Override
    public JavaType typeFromId(DatabindContext context, String id) throws IOException {

        return mapType.computeIfAbsent(id,
                newId->{
                    Class clazz = mapClass.get(newId);
                    Preconditions.checkNotNull(clazz,"The type %d isn't found for base type",newId,baseType);
                    return context.getTypeFactory().constructType(clazz);
                });
    }

    @Override
    public JsonTypeInfo.Id getMechanism() {
        return JsonTypeInfo.Id.CUSTOM;
    }

    public static class TypeIdInfo{
        @JsonProperty("className")
        private final String className;
        @JsonProperty("typeId")
        private final String typeId;
        @JsonProperty("rootClassName")
        private final String rootClassName;

        @JsonCreator
        public TypeIdInfo(@JsonProperty("className") String className, @JsonProperty("typeId") String typeId, @JsonProperty("rootClassName") String rootClassName) {
            this.className = className;
            this.typeId = typeId;
            this.rootClassName = rootClassName;
        }
    }

}
