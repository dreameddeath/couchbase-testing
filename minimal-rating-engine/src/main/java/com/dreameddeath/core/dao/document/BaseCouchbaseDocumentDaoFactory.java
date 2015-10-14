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

package com.dreameddeath.core.dao.document;


import com.dreameddeath.core.dao.counter.CouchbaseCounterDao;
import com.dreameddeath.core.dao.counter.CouchbaseCounterDaoFactory;
import com.dreameddeath.core.dao.exception.dao.DaoNotFoundException;
import com.dreameddeath.core.dao.validation.ValidatorFactory;
import com.dreameddeath.core.model.business.CouchbaseDocument;
import com.dreameddeath.core.model.document.BaseCouchbaseDocument;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;

public class BaseCouchbaseDocumentDaoFactory{
    private Map<Class<? extends BaseCouchbaseDocument>, BaseCouchbaseDocumentDao<?>> daosMap
            = new ConcurrentHashMap<Class<? extends BaseCouchbaseDocument>, BaseCouchbaseDocumentDao<?>>();
    private Map<Pattern,BaseCouchbaseDocumentWithKeyPatternDao<?>> patternsMap
            = new ConcurrentHashMap<Pattern,BaseCouchbaseDocumentWithKeyPatternDao<?>>();

    private ValidatorFactory validatorFactory;
    private CouchbaseCounterDaoFactory counterDaoFactory;

    public void setValidatorFactory(ValidatorFactory factory){
        validatorFactory = factory;
    }
    public ValidatorFactory getValidatorFactory(){
        return validatorFactory;
    }

    public CouchbaseCounterDaoFactory getCounterDaoFactory(){
        return counterDaoFactory;
    }
    public void setCounterDaoFactory(CouchbaseCounterDaoFactory factory){
        counterDaoFactory = factory;
    }

    public void registerCounter(CouchbaseCounterDao counterDao){
        counterDaoFactory.addDao(counterDao);
    }

    public <T extends BaseCouchbaseDocument> void addDaoFor(Class<T> entityClass,BaseCouchbaseDocumentDao<T> dao){
        daosMap.put(entityClass,dao);
        if(dao instanceof BaseCouchbaseDocumentWithKeyPatternDao){
            patternsMap.put(Pattern.compile("^"+((BaseCouchbaseDocumentWithKeyPatternDao) dao).getKeyPattern()+"$"),(BaseCouchbaseDocumentWithKeyPatternDao)dao);
        }
    }

    public <T extends BaseCouchbaseDocument> BaseCouchbaseDocumentDao<T> getDaoForClass(Class<T> entityClass) throws DaoNotFoundException{
        BaseCouchbaseDocumentDao<T> result = (BaseCouchbaseDocumentDao<T>)daosMap.get(entityClass);
        if(result==null){
            Class parentClass=entityClass.getSuperclass();
            if(CouchbaseDocument.class.isAssignableFrom(parentClass)){
                result = getDaoForClass(parentClass.asSubclass(CouchbaseDocument.class));
                if(result!=null){
                    daosMap.put(entityClass,result);
                }
            }
        }
        if(result==null){
            throw new DaoNotFoundException(entityClass);
        }
        return result;
    }

    public BaseCouchbaseDocumentWithKeyPatternDao getDaoForKey(String key) throws DaoNotFoundException {
        for(Pattern pattern:patternsMap.keySet()){
            if(pattern.matcher(key).matches()){
                return patternsMap.get(pattern);
            }
        }
        throw new DaoNotFoundException(key, DaoNotFoundException.Type.DOC);
    }
}