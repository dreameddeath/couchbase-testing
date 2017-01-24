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

package com.dreameddeath.core.model.mapper;

import com.dreameddeath.core.model.document.CouchbaseDocument;
import com.dreameddeath.core.model.exception.mapper.DuplicateMappedEntryInfoException;
import com.dreameddeath.core.model.exception.mapper.MappingNotFoundException;

/**
 * Created by Christophe Jeunesse on 08/06/2015.
 */
public interface IDocumentInfoMapper {
    IKeyMappingInfo getFirstMappingFromKey(String key) throws MappingNotFoundException;
    IKeyMappingInfo getMappingFromKey(String domain,String key) throws MappingNotFoundException;
    <T> T getAttachedClassFromKey(String key, Class<T> clazz) throws MappingNotFoundException;
    <T> T getAttachedClassFromKey(String domain,String key, Class<T> clazz) throws MappingNotFoundException;
    <T> T getAttachedClassFromClass(Class<? extends CouchbaseDocument> clazz, Class<T> classToLookFor) throws MappingNotFoundException;
    <T> T getAttachedClassFromClass(Class<? extends CouchbaseDocument> clazz, Class<T> classToLookFor,String key) throws MappingNotFoundException;
    <T> T getAttachedClassFromClass(Class<? extends CouchbaseDocument> clazz, Class<T> classToLookFor,String domain,String key) throws MappingNotFoundException;

    IDocumentClassMappingInfo getMappingFromClass(Class<? extends CouchbaseDocument> docClass) throws MappingNotFoundException;

    IDocumentClassMappingInfo getMappingFromClass(String domain,Class<? extends CouchbaseDocument> docClass) throws MappingNotFoundException;


    boolean contains(Class<? extends CouchbaseDocument> docClass);
    boolean contains(String domain,Class<? extends CouchbaseDocument> docClass);
    void addRawDocument(Class<? extends CouchbaseDocument> docClass) throws DuplicateMappedEntryInfoException;
    void addDocument(Class<? extends CouchbaseDocument> docClass) throws DuplicateMappedEntryInfoException;

    void addDocument(Class<? extends CouchbaseDocument> docClass,String keyPattern,String domain) throws DuplicateMappedEntryInfoException;
    void addKeyPattern(Class<? extends CouchbaseDocument> docClass,String keyPattern,String domain) throws MappingNotFoundException;


    void addDocument(Class<? extends CouchbaseDocument> docClass,String keyPattern) throws DuplicateMappedEntryInfoException;
    void addKeyPattern(Class<? extends CouchbaseDocument> docClass,String keyPattern) throws MappingNotFoundException;

    void cleanup();
}
