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
    private Map<Class<? extends CouchbaseDocument>, IDocumentClassMappingInfo> perClassInfoMap
            = new ConcurrentHashMap<>();
    private Map<Pattern,IDocumentClassMappingInfo> keyInfoMap
            = new ConcurrentHashMap<>();

    @Override
    public synchronized void addDocument(Class<? extends CouchbaseDocument> docClass) throws DuplicateMappedEntryInfoException {
        addDocument(docClass, ".*");
    }

    @Override
    public synchronized void addDocument(Class<? extends CouchbaseDocument> docClass,String keyPattern) throws DuplicateMappedEntryInfoException {
        if (perClassInfoMap.containsKey(docClass)) {
            throw new DuplicateMappedEntryInfoException("The class <"+docClass+"> is already mapped");
        }

        perClassInfoMap.put(docClass, new DefaultDocumentClassInfoMapping(docClass, null, keyPattern));
        try {
            addKeyPattern(docClass, keyPattern);
        }
        catch(MappingNotFoundException e){
            //Never occurs
        }
    }

    @Override
    public synchronized void addRawDocument(Class<? extends CouchbaseDocument> docClass) throws DuplicateMappedEntryInfoException {
        if (perClassInfoMap.containsKey(docClass)) {
            throw new DuplicateMappedEntryInfoException("The class <"+docClass+"> is already mapped");
        }

        perClassInfoMap.put(docClass, new DefaultDocumentClassInfoMapping(docClass, null, null));
    }


    @Override
    public synchronized void addKeyPattern(Class<? extends CouchbaseDocument> docClass, String keyPattern) throws MappingNotFoundException {
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
        keyInfoMap.put(pattern, getMappingFromClass(docClass));
    }

    protected synchronized IDocumentClassMappingInfo findUsingParent(Class<? extends CouchbaseDocument> docClass){
        if(!perClassInfoMap.containsKey(docClass)) {
            Class superClass = docClass.getSuperclass();
            if(CouchbaseDocument.class.isAssignableFrom(superClass)){
                @SuppressWarnings("unchecked")
                IDocumentClassMappingInfo parentMapping = findUsingParent(superClass);
                if(parentMapping!=null) {
                    DefaultDocumentClassInfoMapping mapper = new DefaultDocumentClassInfoMapping(docClass, parentMapping, parentMapping.keyPattern());
                    perClassInfoMap.put(docClass, mapper);
                }
            }
        }
        return perClassInfoMap.get(docClass);
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
        for(Map.Entry<Pattern,IDocumentClassMappingInfo> entry:keyInfoMap.entrySet()){
            if(entry.getKey().matcher(effectiveKey).matches()){
                return new KeyMappingInfo(prefix,effectiveKey,key,entry.getValue());
            }
        }
        throw new MappingNotFoundException("The key <"+key+"> hasn't been found");
    }

    @Override
    public <T> T getAttachedClassFromKey(String key, Class<T> classToLookFor) throws MappingNotFoundException{
        return getMappingFromKey(key).classMappingInfo().getAttachedObject(classToLookFor);
    }


    @Override
    public <T> T getAttachedClassFromClass(Class<? extends CouchbaseDocument> clazz, Class<T> classToLookFor) throws MappingNotFoundException{
        return getMappingFromClass(clazz).getAttachedObject(classToLookFor);
    }

    @Override
    public <T> T getAttachedClassFromClass(Class<? extends CouchbaseDocument> clazz, Class<T> classToLookFor,String key) throws MappingNotFoundException{
        return getMappingFromClass(clazz).getAttachedObject(classToLookFor,key);
    }


    @Override
    public IDocumentClassMappingInfo getMappingFromClass(Class<? extends CouchbaseDocument> docClass) throws MappingNotFoundException {
        IDocumentClassMappingInfo info = perClassInfoMap.get(docClass);
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

    @Override
    public boolean contains(Class<? extends CouchbaseDocument> docClass) {
        return perClassInfoMap.containsKey(docClass);
    }
}
