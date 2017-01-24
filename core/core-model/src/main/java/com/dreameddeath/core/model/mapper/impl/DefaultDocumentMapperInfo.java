/*
 * Copyright Christophe Jeunesse
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */

package com.dreameddeath.core.model.mapper.impl;

import com.dreameddeath.core.model.document.CouchbaseDocument;
import com.dreameddeath.core.model.entity.model.EntityDef;
import com.dreameddeath.core.model.exception.EntityRequiredInfoNotFoundException;
import com.dreameddeath.core.model.exception.mapper.DuplicateMappedEntryInfoException;
import com.dreameddeath.core.model.exception.mapper.MappingNotFoundException;
import com.dreameddeath.core.model.mapper.IDocumentClassMappingInfo;
import com.dreameddeath.core.model.mapper.IDocumentInfoMapper;
import com.dreameddeath.core.model.mapper.IKeyMappingInfo;
import com.dreameddeath.core.model.util.CouchbaseDocumentStructureReflection;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Christophe Jeunesse on 08/06/2015.
 */
public class DefaultDocumentMapperInfo implements IDocumentInfoMapper{
    public static final String KEY_PREFIX_PATTERN_STR = "(?:(\\w+)\\$)?";
    private final ConcurrentMap<DomainClassKey, IDocumentClassMappingInfo> perClassInfoMap  = new ConcurrentHashMap<>();
    private final ConcurrentMap<DomainKeyPatternKey,IDocumentClassMappingInfo> keyInfoMap = new ConcurrentHashMap<>();

    @Override
    public synchronized void addDocument(Class<? extends CouchbaseDocument> docClass) throws DuplicateMappedEntryInfoException {
        addDocument(docClass, ".*");
    }

    @Override
    public synchronized void addDocument(Class<? extends CouchbaseDocument> docClass,String keyPattern,String domain) throws DuplicateMappedEntryInfoException {
        DomainClassKey key = new DomainClassKey(domain,docClass);
        IDocumentClassMappingInfo oldMappingInfo =perClassInfoMap.putIfAbsent(key, new DefaultDocumentClassInfoMapping(docClass, null, keyPattern));
        if(oldMappingInfo!=null){
            throw new DuplicateMappedEntryInfoException("The class <"+key.toString()+"> is already mapped");
        }
        try {
            addKeyPattern(docClass, keyPattern,domain);
        }
        catch(MappingNotFoundException e){
            //Never occurs
        }
    }

    @Override
    public synchronized void addDocument(Class<? extends CouchbaseDocument> docClass,String keyPattern) throws DuplicateMappedEntryInfoException {
        addDocument(docClass,keyPattern,null);
    }

    @Override
    public synchronized void addRawDocument(Class<? extends CouchbaseDocument> docClass) throws DuplicateMappedEntryInfoException {
        DomainClassKey key = new DomainClassKey(null,docClass);
        if (perClassInfoMap.containsKey(key)) {
            throw new DuplicateMappedEntryInfoException("The class <"+docClass+"> is already mapped");
        }

        perClassInfoMap.put(key, new DefaultDocumentClassInfoMapping(docClass, null, null));
    }


    @Override
    public synchronized void addKeyPattern(Class<? extends CouchbaseDocument> docClass, String keyPattern) throws MappingNotFoundException {
        addKeyPattern(docClass, keyPattern,null);
    }

    @Override
    public synchronized void addKeyPattern(Class<? extends CouchbaseDocument> docClass, String keyPattern,String domain) throws MappingNotFoundException {
        if (keyPattern == null) {
            keyPattern = ".*";
        }
        if(keyPattern.startsWith("^")){
            keyPattern = keyPattern.substring(1);
        }

        keyPattern="^"+KEY_PREFIX_PATTERN_STR+"("+keyPattern;

        if (!keyPattern.endsWith("$")) {
            keyPattern += ")$";
        }
        else{
            keyPattern=keyPattern.substring(0,keyPattern.length())+")$";
        }


        //Pattern pattern = Pattern.compile(keyPattern);
        DomainKeyPatternKey domainKeyPatternKey=new DomainKeyPatternKey(domain,keyPattern);
        keyInfoMap.putIfAbsent(domainKeyPatternKey,getMappingFromClass(domain,docClass));
    }

    protected synchronized IDocumentClassMappingInfo findUsingParent(String domain,Class<? extends CouchbaseDocument> docClass) {
        DomainClassKey key = new DomainClassKey(domain,docClass);
        if(!perClassInfoMap.containsKey(key)) {
            Class superClass = docClass.getSuperclass();
            if(CouchbaseDocument.class.isAssignableFrom(superClass)){
                @SuppressWarnings("unchecked")
                IDocumentClassMappingInfo parentMapping = findUsingParent(domain,superClass);
                if(parentMapping!=null) {
                    DefaultDocumentClassInfoMapping mapper = new DefaultDocumentClassInfoMapping(docClass, parentMapping, parentMapping.keyPattern());
                    perClassInfoMap.put(key, mapper);
                }
            }
        }
        return perClassInfoMap.get(key);
    }

    @Override
    public synchronized void cleanup() {
        perClassInfoMap.clear();
        keyInfoMap.clear();
    }

    @Override
    public synchronized IKeyMappingInfo getFirstMappingFromKey(String key) throws MappingNotFoundException{
        for(Map.Entry<DomainKeyPatternKey,IDocumentClassMappingInfo> entry:keyInfoMap.entrySet()){
            Matcher matcher = entry.getKey().pattern.matcher(key);
            if (matcher.matches()) {
                return new KeyMappingInfo(matcher.group(1), matcher.group(2), key, entry.getValue());
            }
        }
        throw new MappingNotFoundException("The key <"+key+"> hasn't been found");
    }

    @Override
    public synchronized IKeyMappingInfo getMappingFromKey(String domain,String key) throws MappingNotFoundException{
        for(Map.Entry<DomainKeyPatternKey,IDocumentClassMappingInfo> entry:keyInfoMap.entrySet()){
            if(((entry.getKey().domain==null) && (domain==null)) || entry.getKey().domain.equals(domain)) {
                Matcher matcher = entry.getKey().pattern.matcher(key);
                if (matcher.matches()) {
                    return new KeyMappingInfo(matcher.group(1), matcher.group(2), key, entry.getValue());
                }
            }
        }
        throw new MappingNotFoundException("The key <"+key+"> hasn't been found" + (domain!=null?" for domain "+domain:""));
    }

    @Override
    public <T> T getAttachedClassFromKey(String key, Class<T> classToLookFor) throws MappingNotFoundException{
        return getAttachedClassFromKey(null,key,classToLookFor);
    }

    @Override
    public <T> T getAttachedClassFromKey(String domain,String key, Class<T> classToLookFor) throws MappingNotFoundException{
        return getMappingFromKey(domain,key).classMappingInfo().getAttachedObject(classToLookFor);
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
    public <T> T getAttachedClassFromClass(Class<? extends CouchbaseDocument> clazz, Class<T> classToLookFor,String domain,String key) throws MappingNotFoundException{
        return getMappingFromClass(clazz).getAttachedObject(classToLookFor,key,domain);
    }

    @Override
    public IDocumentClassMappingInfo getMappingFromClass(String domain,Class<? extends CouchbaseDocument> docClass) throws MappingNotFoundException {
        DomainClassKey key=new DomainClassKey(domain,docClass);
        IDocumentClassMappingInfo info = perClassInfoMap.get(key);
        if(info==null){
            IDocumentClassMappingInfo infoFromParent = findUsingParent(domain,docClass);
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
    public IDocumentClassMappingInfo getMappingFromClass(Class<? extends CouchbaseDocument> docClass) throws MappingNotFoundException {
        try {
            return getMappingFromClass(null, docClass);
        }
        catch(MappingNotFoundException e){
            try {
                CouchbaseDocumentStructureReflection structureReflection = CouchbaseDocumentStructureReflection.getReflectionFromClass((Class) docClass);
                EntityDef rootEntity = EntityDef.build(structureReflection);
                IDocumentClassMappingInfo info=getMappingFromClass(rootEntity.getModelId().getDomain(),docClass);
                perClassInfoMap.put(new DomainClassKey(null,docClass),info);
                return info;
            }
            catch(MappingNotFoundException|EntityRequiredInfoNotFoundException exceptionForDomain){
                throw e;
            }
        }
    }

    @Override
    public synchronized boolean contains(Class<? extends CouchbaseDocument> docClass) {
        return contains(null,docClass);
    }

    @Override
    public synchronized boolean contains(String domain,Class<? extends CouchbaseDocument> docClass) {
        return perClassInfoMap.containsKey(new DomainClassKey(null,docClass));
    }


    private static class DomainKeyPatternKey{
        private final String patternStr;
        private final String domain;
        private final Pattern pattern;

        public DomainKeyPatternKey(String domain, String patternStr) {
            this.domain = domain;
            this.patternStr=patternStr;
            this.pattern = Pattern.compile(patternStr);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            DomainKeyPatternKey that = (DomainKeyPatternKey) o;

            if (!patternStr.equals(that.patternStr)) return false;
            return domain != null ? domain.equals(that.domain) : that.domain == null;
        }

        @Override
        public int hashCode() {
            int result = patternStr.hashCode();
            result = 31 * result + (domain != null ? domain.hashCode() : 0);
            return result;
        }

        @Override
        public String toString() {
            return "DomainKeyPatternKey{" +
                    "patternStr='" + patternStr + '\'' +
                    ", domain='" + domain + '\'' +
                    '}';
        }
    }

    private static class DomainClassKey{
        private final String domain;
        private final Class<? extends CouchbaseDocument> clazz;

        public DomainClassKey(String domain, Class<? extends CouchbaseDocument> clazz) {
            this.domain = domain;
            this.clazz = clazz;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            DomainClassKey that = (DomainClassKey) o;

            if (domain != null ? !domain.equals(that.domain) : that.domain != null) return false;
            return clazz.equals(that.clazz);
        }

        @Override
        public int hashCode() {
            int result = domain != null ? domain.hashCode() : 0;
            result = 31 * result + clazz.hashCode();
            return result;
        }

        @Override
        public String toString() {
            return "DomainClassKey{" +
                    "domain='" + domain + '\'' +
                    ", clazz=" + clazz +
                    '}';
        }
    }
}
