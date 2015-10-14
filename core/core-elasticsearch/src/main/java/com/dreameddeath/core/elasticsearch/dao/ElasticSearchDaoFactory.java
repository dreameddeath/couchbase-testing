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

package com.dreameddeath.core.elasticsearch.dao;

import com.dreameddeath.core.model.document.CouchbaseDocument;
import com.dreameddeath.core.model.exception.mapper.DuplicateMappedEntryInfoException;
import com.dreameddeath.core.model.exception.mapper.MappingNotFoundException;
import com.dreameddeath.core.model.mapper.IDocumentInfoMapper;

/**
 * Created by Christophe Jeunesse on 08/07/2015.
 */
public class ElasticSearchDaoFactory {
    private final IDocumentInfoMapper documentInfoMapper;

    public ElasticSearchDaoFactory(Builder builder){
        documentInfoMapper = builder.documentInfoMapper;
    }

    public static Builder builder(){
        return new Builder();
    }


    public void addDaoForClass(Class<? extends CouchbaseDocument> docClass,ElasticSearchDao dao){
        if(!documentInfoMapper.contains(docClass)){
            try {
                documentInfoMapper.addRawDocument(docClass);
            }
            catch(DuplicateMappedEntryInfoException e){
                //Should not occur
            }
        }

        try {
            documentInfoMapper.getMappingFromClass(docClass).attachObject(ElasticSearchDao.class, dao);
        }
        catch(MappingNotFoundException e){
            //Should not occur
        }
    }

    public void addDaoForClassAndPattern(Class<? extends CouchbaseDocument> docClass,String pattern,ElasticSearchDao dao){
        if(!documentInfoMapper.contains(docClass)){
            try {
                documentInfoMapper.addDocument(docClass, pattern);
            }
            catch(DuplicateMappedEntryInfoException e){
                //Should not occur
            }
        }

        try {
            documentInfoMapper.getMappingFromClass(docClass).attachObject(ElasticSearchDao.class, dao);
        }
        catch(MappingNotFoundException e){
            //Should not occur
        }
    }


    public <T extends CouchbaseDocument> ElasticSearchDao<T> getDaoForClass(Class<T> clazz) throws MappingNotFoundException{
        return documentInfoMapper.getMappingFromClass(clazz).getAttachedObject(ElasticSearchDao.class);
    }


    public static class Builder{
        private IDocumentInfoMapper documentInfoMapper;

        public Builder withDocumentInfoMappper(IDocumentInfoMapper mapper){
            documentInfoMapper = mapper;
            return this;
        }

        public ElasticSearchDaoFactory build(){
            return new ElasticSearchDaoFactory(this);
        }
    }
}
