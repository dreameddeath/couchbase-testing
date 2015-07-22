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


import com.dreameddeath.core.couchbase.impl.GenericCouchbaseTranscoder;
import com.dreameddeath.core.dao.annotation.DaoForClass;
import com.dreameddeath.core.dao.counter.CouchbaseCounterDao;
import com.dreameddeath.core.dao.counter.CouchbaseCounterDaoFactory;
import com.dreameddeath.core.dao.exception.DaoNotFoundException;
import com.dreameddeath.core.dao.unique.CouchbaseUniqueKeyDao;
import com.dreameddeath.core.dao.unique.CouchbaseUniqueKeyDaoFactory;
import com.dreameddeath.core.dao.view.CouchbaseViewDao;
import com.dreameddeath.core.dao.view.CouchbaseViewDaoFactory;
import com.dreameddeath.core.model.document.CouchbaseDocument;
import com.dreameddeath.core.model.exception.mapper.DuplicateMappedEntryInfoException;
import com.dreameddeath.core.model.exception.mapper.MappingNotFoundException;
import com.dreameddeath.core.model.mapper.IDocumentClassMappingInfo;
import com.dreameddeath.core.model.mapper.IDocumentInfoMapper;
import com.dreameddeath.core.model.mapper.impl.DefaultDocumentMapperInfo;
import com.dreameddeath.core.model.transcoder.ITranscoder;

public class CouchbaseDocumentDaoFactory {
    private final CouchbaseCounterDaoFactory _counterDaoFactory;
    private final CouchbaseUniqueKeyDaoFactory _uniqueKeyDaoFactory;
    private final CouchbaseViewDaoFactory _viewDaoFactory;
    private final IDocumentInfoMapper _documentInfoMapper;

    public CouchbaseDocumentDaoFactory(Builder builder){
        _documentInfoMapper = builder._documentInfoMapper;
        _counterDaoFactory = builder._counterDaoFactoryBuilder.build();
        _uniqueKeyDaoFactory = builder._uniqueKeyDaoFactoryBuilder.build();
        _viewDaoFactory = builder._viewDaoFactoryBuilder.build();
    }

    public static Builder builder(){
        return new Builder();
    }

    public static class Builder{
        private CouchbaseCounterDaoFactory.Builder _counterDaoFactoryBuilder;
        private CouchbaseUniqueKeyDaoFactory.Builder _uniqueKeyDaoFactoryBuilder;
        private CouchbaseViewDaoFactory.Builder _viewDaoFactoryBuilder;
        private IDocumentInfoMapper _documentInfoMapper;

        public Builder(){
            _documentInfoMapper = new DefaultDocumentMapperInfo();
            _counterDaoFactoryBuilder = CouchbaseCounterDaoFactory.builder().withDocumentInfoMapper(_documentInfoMapper);
            _uniqueKeyDaoFactoryBuilder = CouchbaseUniqueKeyDaoFactory.builder().withDocumentInfoMapper(_documentInfoMapper);
            _viewDaoFactoryBuilder = CouchbaseViewDaoFactory.builder();
        }

        public Builder withDocumentInfoMapper(IDocumentInfoMapper documentInfoMapper){
            _documentInfoMapper = documentInfoMapper;
            _counterDaoFactoryBuilder.withDocumentInfoMapper(documentInfoMapper);
            _uniqueKeyDaoFactoryBuilder.withDocumentInfoMapper(documentInfoMapper);
            return this;
        }

        public CouchbaseCounterDaoFactory.Builder getCounterDaoFactoryBuilder() {
            return _counterDaoFactoryBuilder;
        }

        public CouchbaseUniqueKeyDaoFactory.Builder getUniqueKeyDaoFactoryBuilder() {
            return _uniqueKeyDaoFactoryBuilder;
        }

        public CouchbaseViewDaoFactory.Builder getViewDaoFactoryBuilder() {
            return _viewDaoFactoryBuilder;
        }

        public IDocumentInfoMapper getDocumentInfoMapper() {
            return _documentInfoMapper;
        }

        public CouchbaseDocumentDaoFactory build(){
            return new CouchbaseDocumentDaoFactory(this);
        }
    }


    public CouchbaseViewDaoFactory getViewDaoFactory() {
        return _viewDaoFactory;
    }


    public CouchbaseCounterDaoFactory getCounterDaoFactory(){
        return _counterDaoFactory;
    }

    public CouchbaseUniqueKeyDaoFactory getUniqueKeyDaoFactory(){
        return _uniqueKeyDaoFactory;
    }


    public void registerCounter(CouchbaseCounterDao counterDao){
        _counterDaoFactory.addDao(counterDao);
    }

    public <T extends CouchbaseDocument> void addDao(CouchbaseDocumentDao<T> dao,ITranscoder<T> transcoder) throws DuplicateMappedEntryInfoException{
        DaoForClass annotation = dao.getClass().getAnnotation(DaoForClass.class);
        if(annotation==null){
            throw new NullPointerException("Annotation DaoForClass not defined for dao <"+dao.getClass().getName()+">");
        }
        dao.setTranscoder(new GenericCouchbaseTranscoder<>(transcoder,dao.getBucketDocumentClass()));
        addDaoFor((Class<T>) annotation.value(), dao);
    }

    public <T extends CouchbaseDocument> void addDaoFor(Class<T> entityClass,CouchbaseDocumentDao<T> dao) throws DuplicateMappedEntryInfoException{
        String pattern = (dao instanceof CouchbaseDocumentWithKeyPatternDao)?((CouchbaseDocumentWithKeyPatternDao) dao).getKeyPattern():".*";

        if(!_documentInfoMapper.contains(entityClass)){
            _documentInfoMapper.addDocument(entityClass,pattern);
        }
        try {
            IDocumentClassMappingInfo info = _documentInfoMapper.getMappingFromClass(entityClass);
            info.attachObject(CouchbaseDocumentDao.class, dao);
            info.attachObject(ITranscoder.class,dao.getTranscoder().getTranscoder());
        }
        catch(MappingNotFoundException e){

        }

        for(CouchbaseCounterDao.Builder daoCounterBuilder:dao.getCountersBuilder()){
            registerCounter(daoCounterBuilder.build());
        }
        for(CouchbaseUniqueKeyDao.Builder daoUniqueKeyBuilder:dao.getUniqueKeysBuilder()){
            _uniqueKeyDaoFactory.addDaoFor(daoUniqueKeyBuilder.getNameSpace(),daoUniqueKeyBuilder.build());
        }
        for(CouchbaseViewDao daoView:dao.getViewDaos()){
            _viewDaoFactory.addDaoFor(entityClass,daoView);
        }

    }

    public <T extends CouchbaseDocument> CouchbaseDocumentDao<T> getDaoForClass(Class<T> entityClass) throws DaoNotFoundException{
        try {
            IDocumentClassMappingInfo info = _documentInfoMapper.getMappingFromClass(entityClass);
            CouchbaseDocumentDao<T> result = info.getAttachedObject(CouchbaseDocumentDao.class);
            if(result==null){
                throw new DaoNotFoundException(entityClass);
            }
            return result;
        }
        catch(MappingNotFoundException e){
            throw new DaoNotFoundException(entityClass);
        }
    }

    public CouchbaseDocumentWithKeyPatternDao getDaoForKey(String key) throws DaoNotFoundException {
        try {
            return _documentInfoMapper.getMappingFromKey(key).classMappingInfo().getAttachedObject(CouchbaseDocumentWithKeyPatternDao.class);
        }
        catch(MappingNotFoundException e){
            throw new DaoNotFoundException(key, DaoNotFoundException.Type.DOC);
        }
    }
}