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

import java.util.Set;

/**
 * Created by Christophe Jeunesse on 08/06/2015.
 */
public interface IDocumentClassMappingInfo {
    Class<? extends CouchbaseDocument> classInfo();
    Class<? extends CouchbaseDocument> classRootInfo();
    String keyPattern();
    Set<Class<? extends CouchbaseDocument>> getChildClasses();
    void addChildClass(Class<? extends CouchbaseDocument> childDocClass);

    <T> T getAttachedObject(Class<T> clazz);
    <T> T getAttachedObject(Class<T> clazz,String key);
    <T> T getAttachedObject(Class<T> clazz,String domain,String key);
    <T> void attachObject(Class<T> clazz,Object obj);
    <T> void attachObject(Class<T> clazz,String pattern,Object obj);
    <T> void attachObject(Class<T> clazz,String pattern,String domain,Object obj);

}
