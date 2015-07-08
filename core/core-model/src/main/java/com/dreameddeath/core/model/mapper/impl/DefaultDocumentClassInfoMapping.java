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
    private final Class<? extends CouchbaseDocument> _clazz;
    private final IDocumentClassMappingInfo _parent;
    private final Class<? extends CouchbaseDocument> _clazzRoot;
    private final Set<Class<? extends CouchbaseDocument>> _childClasses =new HashSet<>();
    private final String _keyPattern;
    private final Map<Class,Object> _attachedInfo =new ConcurrentHashMap<>();
    private final Map<KeyClassTuple,Object> _perKeyAttachedInfo =new ConcurrentHashMap<>();

    private class KeyClassTuple{
        private final String _patternStr;
        private Pattern _pattern;
        private final Class _class;
        private KeyClassTuple(String pattern,Class clazz){
            _patternStr = pattern;
            _class = clazz;
        }

        public boolean matches(String key,Class clazz){
            if(_pattern==null){
                _pattern = Pattern.compile("^"+_patternStr+"$");
            }
            return _pattern.matcher(key).matches() && _class.equals(clazz);
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
            return _class.equals(target._class) && _pattern.equals(target._pattern);
        }
    }

    public DefaultDocumentClassInfoMapping(Class<? extends CouchbaseDocument> clazz, IDocumentClassMappingInfo parent, String pattern){
        _clazz = clazz;
        _keyPattern = pattern;
        if(parent !=null){
            _clazzRoot = parent.classRootInfo();
            _parent = parent;
            _parent.addChildClass(clazz);
        }
        else{
            _parent = null;
            _clazzRoot = _clazz;
        }
    }

    @Override
     public synchronized void addChildClass(Class<? extends CouchbaseDocument> childDocClass) {
        _childClasses.add(childDocClass);
        if(_parent!=null){
            _parent.addChildClass(childDocClass);
        }
    }

    @Override
    public <T> T getAttachedObject(Class<T> clazz) {
        @SuppressWarnings("unchecked")
        T res = (T)_attachedInfo.get(clazz);
        if(res==null && _parent!=null){
            T resParent = _parent.getAttachedObject(clazz);
            _attachedInfo.putIfAbsent(clazz,resParent);
            return resParent;
        }
        return res;
    }


    @Override
    public <T> T getAttachedObject(Class<T> clazz,String key) {
        for(Map.Entry<KeyClassTuple,Object> element:_perKeyAttachedInfo.entrySet()){
            if(element.getKey().matches(key, clazz)){
                return (T)element.getValue();
            }
        }
        if(_parent!=null){
            return _parent.getAttachedObject(clazz,key);
        }
        return null;
    }


    @Override
    public synchronized <T> void attachObject(Class<T> clazz, Object obj) {
        _attachedInfo.put(clazz,obj);
    }

    @Override
    public synchronized <T> void attachObject(Class<T> clazz,String pattern, Object obj) {
        _perKeyAttachedInfo.put(new KeyClassTuple(pattern,clazz),obj);
    }



    @Override
    public Set<Class<? extends CouchbaseDocument>> getChildClasses(){
        return _childClasses;
    }

    @Override
    public Class<? extends CouchbaseDocument> classInfo() {
        return _clazz;
    }

    @Override
    public Class<? extends CouchbaseDocument> classRootInfo() {
        return _clazzRoot;
    }

    @Override
    public String keyPattern() {
        return _keyPattern;
    }



}
