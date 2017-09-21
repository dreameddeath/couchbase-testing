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

package com.dreameddeath.core.notification.utils;

import com.dreameddeath.compile.tools.annotation.processor.reflection.ClassInfo;
import com.dreameddeath.core.depinjection.IDependencyInjector;
import com.dreameddeath.core.json.ObjectMapperFactory;
import com.dreameddeath.core.notification.annotation.Listener;
import com.dreameddeath.core.notification.listener.IEventListener;
import com.dreameddeath.core.notification.listener.IEventListenerBuilder;
import com.dreameddeath.core.notification.listener.IEventListenerTypeMatcher;
import com.dreameddeath.core.notification.model.v1.listener.ListenerDescription;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Preconditions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

import java.io.IOException;
import java.io.InputStream;
import java.io.Writer;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by Christophe Jeunesse on 16/08/2016.
 */
public class ListenerInfoManager {
    private static Logger LOG = LoggerFactory.getLogger(ListenerInfoManager.class);
    public static final String ROOT_PATH="META-INF/core-notification";
    public static final String TYPES_DEF_PATH="types";
    public static final String CLASSES_DEF_PATH="classes";

    private ObjectMapper mapper = ObjectMapperFactory.BASE_INSTANCE.getMapper();
    private Map<String,Class> classMapper = new ConcurrentHashMap<>();


    public String getFilenameFromType(String type){
        return String.format("%s/%s/%s.json", ROOT_PATH, TYPES_DEF_PATH, type.toLowerCase());
    }

    public String getFilenameFromClass(ClassInfo classInfo) {
        return String.format("%s/%s/%s.json", ROOT_PATH, CLASSES_DEF_PATH, classInfo.getFullName().toLowerCase());
    }


    public void buildTypeDefinitionFile(Writer writer, ClassInfo listenerClassInfo, Listener annot, String type) throws IOException {
        mapper.writeValue(writer,new ListenerTypeInfo(listenerClassInfo.getFullName(),type));
    }

    public void buildClassDefinitionFile(Writer writer, ClassInfo classInfo, Listener annot) throws IOException{
        mapper.writeValue(writer,new ListenerClassInfo(classInfo.getFullName()));
    }

    public static class ListenerClassInfo{
        @JsonProperty
        public final String className;

        @JsonCreator
        public ListenerClassInfo(@JsonProperty("className") String className) {
            this.className = className;
        }
    }


    public static class ListenerTypeInfo{
        @JsonProperty
        public final String className;
        @JsonProperty
        public final String type;

        @JsonCreator
        public ListenerTypeInfo(@JsonProperty("className") String className, @JsonProperty("type") String type) {
            this.className = className;
            this.type = type;
        }
    }

    public ListenerTypeInfo getTypeDefinition(String type){
        String filename = getFilenameFromType(type);
        InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream(filename);
        if (is == null) {
            throw new RuntimeException("Cannot find/read file <" + filename + "> for type <" + type + ">");
        }
        try {
            return mapper.readValue(is, ListenerTypeInfo.class);
        } catch (IOException e) {
            throw new RuntimeException("Cannot find/read file <" + filename + "> for type <" + type + ">", e);
        }
    }

    public synchronized List<ListenerClassInfo> getListenersClassInfo(){
        try {
            PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
            Resource[] resultResources= resolver.getResources("classpath*:" + ROOT_PATH + "/" + CLASSES_DEF_PATH + "/*.json");
            List<ListenerClassInfo> result = new ArrayList<>(resultResources.length);
            for(Resource classInfoResource:resultResources){
                try {
                    result.add(mapper.readValue(classInfoResource.getInputStream(), ListenerClassInfo.class));
                }
                catch(Throwable e){
                    LOG.error("Cannot read listener class info file <"+classInfoResource.getFile().getAbsolutePath()+">",e);
                    throw new RuntimeException("Cannot read listener class info file <"+classInfoResource.getFilename()+">",e);
                }
            }
            return result;
        }
        catch(Exception e) {
            throw new RuntimeException(e);
        }
    }

    public Class<? extends IEventListener> getListenerClass(ListenerClassInfo classInfo){
        try {
            return (Class<? extends IEventListener>)Thread.currentThread().getContextClassLoader().loadClass(classInfo.className);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    public IEventListenerTypeMatcher getTypeMatcher(ListenerClassInfo classInfo){
        Class<? extends IEventListener> listenerClass = getListenerClass(classInfo);
        Listener annot = listenerClass.getAnnotation(Listener.class);
        Preconditions.checkNotNull(annot,"Cannot find listener annotation from class {}",listenerClass.getName());
        try {
            return annot.matcher().getConstructor(Class.class).newInstance(listenerClass);
        } catch (InstantiationException | IllegalAccessException | NoSuchMethodException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }

    public IEventListenerBuilder getListenerBuilder(ListenerClassInfo classInfo,IDependencyInjector dependencyInjector){
        Class<IEventListener> listenerClass = (Class<IEventListener>)getListenerClass(classInfo);
        Constructor<IEventListener> descriptionBasedContructor;
        Constructor<IEventListener> paramsBasedContructor;

        try {
            descriptionBasedContructor = listenerClass.getConstructor(ListenerDescription.class);
        } catch (NoSuchMethodException e) {
            descriptionBasedContructor=null;
        }

        try {
            paramsBasedContructor = listenerClass.getConstructor(String.class,String.class,String.class,Map.class);
        } catch (NoSuchMethodException e) {
            paramsBasedContructor=null;
        }

        return constructBuilder(listenerClass,descriptionBasedContructor,paramsBasedContructor,dependencyInjector);
    }

    private <T extends IEventListener> IEventListenerBuilder constructBuilder(Class<T> listenerClass,
                                                   final Constructor<T> descriptionBasedContructor,
                                                   final Constructor<T> paramsBasedContructor,
                                                   IDependencyInjector dependencyInjector)
    {
        Preconditions.checkArgument(descriptionBasedContructor!=null || paramsBasedContructor!=null,"Cannot find acceptable constructor for class {}",listenerClass.getName());
        if(descriptionBasedContructor!=null && paramsBasedContructor!=null){
            return new IEventListenerBuilder() {
                @Override
                public IEventListener build(String domain,String type, String name, Map<String, String> params) {
                    try {
                        T listener = paramsBasedContructor.newInstance(domain,type,name,params);
                        return dependencyInjector.autowireBean(listener,"listener"+name);
                    } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
                        throw new RuntimeException(e);
                    }
                }

                @Override
                public IEventListener build(ListenerDescription description) {
                    try {
                        T listener = descriptionBasedContructor.newInstance(description);
                        return dependencyInjector.autowireBean(listener,"listener"+description.getName());
                    } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
                        throw new RuntimeException(e);
                    }
                }
            };
        }
        else if(descriptionBasedContructor!=null){
            return new IEventListenerBuilder() {
                @Override
                public IEventListener build(String domain,String type, String name, Map<String, String> params) {
                    throw new RuntimeException("Cannot buildFromInternal using parameters for class "+descriptionBasedContructor.getDeclaringClass().getName());
                }

                @Override
                public IEventListener build(ListenerDescription description) {
                    try {
                        T listener = descriptionBasedContructor.newInstance(description);
                        return dependencyInjector.autowireBean(listener,"listener"+description.getName());
                    } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
                        throw new RuntimeException(e);
                    }
                }
            };
        }
        else if(paramsBasedContructor!=null){
            return new IEventListenerBuilder() {
                @Override
                public IEventListener build(String domain,String type, String name, Map<String, String> params) {
                    try {
                        T listener = paramsBasedContructor.newInstance(domain,type,name,params);
                        return dependencyInjector.autowireBean(listener,"listener"+name);
                    } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
                        throw new RuntimeException(e);
                    }
                }

                @Override
                public IEventListener build(ListenerDescription description) {
                    return build(description.getDomain(),description.getType(),description.getName(),description.getParameters());
                }
            };
        }
        return null;
    }
}