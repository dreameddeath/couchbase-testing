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

package com.dreameddeath.core.model.mapper.impl;

import com.dreameddeath.core.model.document.CouchbaseDocument;
import com.dreameddeath.core.model.mapper.IDocumentClassMappingInfo;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;

/**
 * Created by Christophe Jeunesse on 08/06/2015.
 */
public class DefaultDocumentClassInfoMapping implements IDocumentClassMappingInfo {
    private final Class<? extends CouchbaseDocument> clazz;
    private final IDocumentClassMappingInfo parent;
    private final Class<? extends CouchbaseDocument> clazzRoot;
    private final Set<Class<? extends CouchbaseDocument>> childClasses =new HashSet<>();
    private final String keyPattern;
    private final Map<Class,Object> attachedInfo =new ConcurrentHashMap<>();
    private final Map<KeyClassTuple,Object> perKeyAttachedInfo =new ConcurrentHashMap<>();

    private class KeyClassTuple{
        private final String patternStr;
        private Pattern pattern;
        private final Class clazz;
        private KeyClassTuple(String pattern,Class clazz){
            patternStr = pattern;
            this.clazz = clazz;
        }

        public boolean matches(String key,Class clazz){
            if(pattern==null){
                pattern = Pattern.compile("^"+patternStr+"$");
            }
            return pattern.matcher(key).matches() && this.clazz.equals(clazz);
        }


        @Override
        public boolean equals(Object obj){
            if(this==obj){
                return true;
            }
            else if(obj == null){
                return false;
            }
            else if (! (obj instanceof KeyClassTuple)){
                return false;
            }
            KeyClassTuple target = (KeyClassTuple)obj;
            return clazz.equals(target.clazz) && pattern.equals(target.pattern);
        }
    }

    public DefaultDocumentClassInfoMapping(Class<? extends CouchbaseDocument> clazz, IDocumentClassMappingInfo parent, String pattern){
        this.clazz = clazz;
        keyPattern = pattern;
        if(parent !=null){
            clazzRoot = parent.classRootInfo();
            this.parent = parent;
            parent.addChildClass(clazz);
        }
        else{
            this.parent = null;
            clazzRoot = clazz;
        }
    }

    @Override
     public synchronized void addChildClass(Class<? extends CouchbaseDocument> childDocClass) {
        childClasses.add(childDocClass);
        if(parent!=null){
            parent.addChildClass(childDocClass);
        }
    }

    @Override
    public <T> T getAttachedObject(Class<T> clazz) {
        @SuppressWarnings("unchecked")
        T res = (T)attachedInfo.get(clazz);
        if(res==null && parent!=null){
            T resParent = parent.getAttachedObject(clazz);
            attachedInfo.putIfAbsent(clazz, resParent);
            return resParent;
        }
        return res;
    }

    @Override
    public <T> T getAttachedObject(Class<T> clazz,String key) {
        for(Map.Entry<KeyClassTuple,Object> element:perKeyAttachedInfo.entrySet()){
            if(element.getKey().matches(key, clazz)){
                return (T)element.getValue();
            }
        }
        if(parent!=null){
            return parent.getAttachedObject(clazz,key);
        }
        return null;
    }


    @Override
    public synchronized <T> void attachObject(Class<T> clazz, Object obj) {
        attachedInfo.put(clazz,obj);
    }

    @Override
    public synchronized <T> void attachObject(Class<T> clazz,String pattern, Object obj) {
        perKeyAttachedInfo.put(new KeyClassTuple(pattern,clazz),obj);
    }

    @Override
    public Set<Class<? extends CouchbaseDocument>> getChildClasses(){
        return childClasses;
    }

    @Override
    public Class<? extends CouchbaseDocument> classInfo() {
        return clazz;
    }

    @Override
    public Class<? extends CouchbaseDocument> classRootInfo() {
        return clazzRoot;
    }

    @Override
    public String keyPattern() {
        return keyPattern;
    }
}
