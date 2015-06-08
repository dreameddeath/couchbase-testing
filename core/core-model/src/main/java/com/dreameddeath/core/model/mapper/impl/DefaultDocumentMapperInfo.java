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
import com.dreameddeath.core.model.exception.mapper.DuplicateMappedEntryInfoException;
import com.dreameddeath.core.model.exception.mapper.MappingNotFoundException;
import com.dreameddeath.core.model.mapper.IDocumentClassMappingInfo;
import com.dreameddeath.core.model.mapper.IDocumentInfoMapper;
import com.dreameddeath.core.model.mapper.IKeyMappingInfo;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;

/**
 * Created by Christophe Jeunesse on 08/06/2015.
 */
public class DefaultDocumentMapperInfo implements IDocumentInfoMapper{
    private Map<Class<? extends CouchbaseDocument>, IDocumentClassMappingInfo> _perClassInfoMap
            = new ConcurrentHashMap<>();
    private Map<Pattern,IDocumentClassMappingInfo> _keyInfoMap
            = new ConcurrentHashMap<>();


    public synchronized void addDocumentInfo(Class<? extends CouchbaseDocument> docClass) throws DuplicateMappedEntryInfoException {
        addDocumentInfo(docClass, ".*");
    }

    public synchronized void addDocumentInfo(Class<? extends CouchbaseDocument> docClass,String keyPattern) throws DuplicateMappedEntryInfoException {
        if (_perClassInfoMap.containsKey(docClass)) {
            throw new DuplicateMappedEntryInfoException("The class <"+docClass+"> is already mapped");
        }

        if (keyPattern == null) {
            keyPattern = ".*";
        }
        if (!keyPattern.startsWith("^")) {
            keyPattern = "^" + keyPattern;
        }
        if (!keyPattern.endsWith("$")) {
            keyPattern += "$";
        }


        Pattern pattern = Pattern.compile(keyPattern);
        _perClassInfoMap.put(docClass, new DefaultDocumentClassInfoMapping(docClass,null,keyPattern));
        _keyInfoMap.put(pattern, _perClassInfoMap.get(docClass));
    }

    protected synchronized IDocumentClassMappingInfo findUsingParent(Class<? extends CouchbaseDocument> docClass){
        if(!_perClassInfoMap.containsKey(docClass)) {
            Class superClass = docClass.getSuperclass();
            if(CouchbaseDocument.class.isAssignableFrom(superClass)){
                @SuppressWarnings("unchecked")
                IDocumentClassMappingInfo parentMapping = findUsingParent(superClass);
                if(parentMapping!=null) {
                    DefaultDocumentClassInfoMapping mapper = new DefaultDocumentClassInfoMapping(docClass, parentMapping, parentMapping.keyPattern());
                    _perClassInfoMap.put(docClass, mapper);
                }
            }
        }
        return _perClassInfoMap.get(docClass);
    }

    @Override
    public IKeyMappingInfo getMappingFromKey(String key) throws MappingNotFoundException{
        String effectiveKey = key;
        String prefix =null;
        if(key.contains("$")){
            int pos=key.indexOf("$");
            prefix = key.substring(0,pos);
            effectiveKey = key.substring(pos+1);
        }
        for(Map.Entry<Pattern,IDocumentClassMappingInfo> entry:_keyInfoMap.entrySet()){
            if(entry.getKey().matcher(effectiveKey).matches()){
                return new KeyMappingInfo(prefix,effectiveKey,key,entry.getValue());
            }
        }
        throw new MappingNotFoundException("The key <"+key+"> hasn't been found");
    }

    @Override
    public IDocumentClassMappingInfo getMappingFromClass(Class<? extends CouchbaseDocument> docClass) throws MappingNotFoundException {
        IDocumentClassMappingInfo info = _perClassInfoMap.get(docClass);
        if(info==null){
            IDocumentClassMappingInfo infoFromParent = findUsingParent(docClass);
            if(infoFromParent==null){
                throw new MappingNotFoundException("Cannot find class <"+docClass+">");
            }
            return infoFromParent;
        }
        else{
            return info;
        }
    }
}
