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

package com.dreameddeath.core.dao.factory;


import com.dreameddeath.core.config.exception.ConfigPropertyValueNotFoundException;
import com.dreameddeath.core.couchbase.ICouchbaseBucket;
import com.dreameddeath.core.couchbase.ICouchbaseBucketFactory;
import com.dreameddeath.core.couchbase.exception.TranscoderNotFoundException;
import com.dreameddeath.core.couchbase.impl.CouchbaseBucketFactory;
import com.dreameddeath.core.couchbase.utils.CouchbaseUtils;
import com.dreameddeath.core.dao.annotation.DaoForClass;
import com.dreameddeath.core.dao.config.CouchbaseDaoConfigProperties;
import com.dreameddeath.core.dao.counter.CouchbaseCounterDao;
import com.dreameddeath.core.dao.document.CouchbaseDocumentDao;
import com.dreameddeath.core.dao.document.IDaoWithKeyPattern;
import com.dreameddeath.core.dao.exception.DaoNotFoundException;
import com.dreameddeath.core.dao.model.discovery.DaoInstanceInfo;
import com.dreameddeath.core.dao.model.utils.DaoInfo;
import com.dreameddeath.core.dao.registrar.DaoRegistrar;
import com.dreameddeath.core.dao.unique.CouchbaseUniqueKeyDao;
import com.dreameddeath.core.dao.view.CouchbaseViewDao;
import com.dreameddeath.core.dao.view.CouchbaseViewDaoFactory;
import com.dreameddeath.core.java.utils.ClassUtils;
import com.dreameddeath.core.model.annotation.DocumentEntity;
import com.dreameddeath.core.model.document.CouchbaseDocument;
import com.dreameddeath.core.model.entity.EntityDefinitionManager;
import com.dreameddeath.core.model.entity.model.EntityDef;
import com.dreameddeath.core.model.entity.model.EntityModelId;
import com.dreameddeath.core.model.exception.mapper.DuplicateMappedEntryInfoException;
import com.dreameddeath.core.model.exception.mapper.MappingNotFoundException;
import com.dreameddeath.core.model.mapper.IDocumentClassMappingInfo;
import com.dreameddeath.core.model.mapper.IDocumentInfoMapper;
import com.dreameddeath.core.model.mapper.impl.DefaultDocumentMapperInfo;
import com.dreameddeath.core.model.transcoder.ITranscoder;
import com.dreameddeath.core.model.util.CouchbaseDocumentStructureReflection;
import com.google.common.base.Preconditions;
import org.apache.curator.framework.CuratorFramework;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class CouchbaseDocumentDaoFactory implements IDaoFactory {
    private static final Logger LOG= LoggerFactory.getLogger(CouchbaseDocumentDaoFactory.class);
    private final EntityDefinitionManager entityDefinitionManager=new EntityDefinitionManager();
    private final ICouchbaseBucketFactory bucketFactory;
    private final List<CouchbaseDocumentDao<?>> attachedDaoList=new ArrayList<>();
    private final CouchbaseCounterDaoFactory counterDaoFactory;
    private final CouchbaseUniqueKeyDaoFactory uniqueKeyDaoFactory;
    private final CouchbaseViewDaoFactory viewDaoFactory;
    private final IDocumentInfoMapper documentInfoMapper;
    private final DaoRegistrar daoRegistrar;

    public CouchbaseDocumentDaoFactory(Builder builder){
        if(builder.couchbaseBucketFactory==null){
            bucketFactory = CouchbaseBucketFactory.builder().build();
        }
        else {
            bucketFactory = builder.couchbaseBucketFactory;
        }
        documentInfoMapper = builder.documentInfoMapper;
        counterDaoFactory = builder.counterDaoFactoryBuilder.build();
        uniqueKeyDaoFactory = builder.uniqueKeyDaoFactoryBuilder.build();
        viewDaoFactory = builder.viewDaoFactoryBuilder.build();
        if(builder.curatorFramework!=null) {
            daoRegistrar = new DaoRegistrar(builder.curatorFramework,builder.daemonUid,builder.webServerUid);
        }
        else{
            daoRegistrar=null;
        }
    }

    public CouchbaseViewDaoFactory getViewDaoFactory() {
        return viewDaoFactory;
    }

    public CouchbaseCounterDaoFactory getCounterDaoFactory(){
        return counterDaoFactory;
    }

    public CouchbaseUniqueKeyDaoFactory getUniqueKeyDaoFactory(){
        return uniqueKeyDaoFactory;
    }

    public ICouchbaseBucketFactory getBucketFactory() {
        return bucketFactory;
    }

    public IDocumentInfoMapper getDocumentInfoMapper() {
        return documentInfoMapper;
    }

    public <T extends CouchbaseDocument> void addDaoFor(Class<T> entityClass,CouchbaseDocumentDao<T> dao) throws DuplicateMappedEntryInfoException{
        if(dao.getDomain()==null){
            DocumentEntity documentEntity = entityClass.getAnnotation(DocumentEntity.class);
            if(documentEntity!=null) {
                dao.setDomain(EntityModelId.build(documentEntity,entityClass).getDomain());
            }
        }

        dao.setEntityManager(entityDefinitionManager);
        Preconditions.checkArgument(dao.getDomain()!=null,"The dao %s for entity %s must belong to a given domain",dao.getClass().getName(),entityClass.getName());
        dao.init();
        attachedDaoList.add(dao);
        final String pattern;
        if(dao instanceof IDaoWithKeyPattern){
            pattern = ((IDaoWithKeyPattern) dao).getKeyPattern().getKeyPatternStr();
        }
        else{
            pattern=".*";
        }

        if (!documentInfoMapper.contains(dao.getDomain(), entityClass)) {
            documentInfoMapper.addDocument(entityClass, pattern, dao.getDomain());
        }

        try {

            IDocumentClassMappingInfo info = documentInfoMapper.getMappingFromClass(dao.getDomain(),entityClass);
            info.attachObject(CouchbaseDocumentDao.class, dao);
            info.attachObject(ITranscoder.class, CouchbaseUtils.resolveTranscoderForClass(entityClass));
        }
        catch(MappingNotFoundException|TranscoderNotFoundException e){
            throw new RuntimeException(e);///TODO improve errors
        }

        for(CouchbaseCounterDao.Builder daoCounterBuilder:dao.getCountersBuilder()){
            counterDaoFactory.addDao(daoCounterBuilder.build());
        }
        for(CouchbaseUniqueKeyDao.Builder daoUniqueKeyBuilder:dao.getUniqueKeysBuilder()){
            uniqueKeyDaoFactory.addDaoFor(daoUniqueKeyBuilder.getNameSpace(),daoUniqueKeyBuilder.build());
        }
        for(CouchbaseViewDao daoView:dao.getViewDaos()){
            viewDaoFactory.addDaoFor(entityClass, daoView);
        }
        if(daoRegistrar!=null){
            try {
                DaoInstanceInfo daoInstanceInfo = new DaoInstanceInfo(dao);
                daoRegistrar.enrich(daoInstanceInfo);
                daoRegistrar.register(daoInstanceInfo);
            }
            catch(Exception e){
                LOG.error("Cannot register dao <"+dao.getClass().getName()+">",e);
            }
        }
    }


    public List<DaoInstanceInfo> getRegisteredDaoInstancesInfo(){
        if(daoRegistrar!=null){
            return daoRegistrar.registeredList();
        }
        else{
            return Collections.emptyList();
        }
    }

    public <T extends CouchbaseDocument> void addDao(CouchbaseDocumentDao<T> dao) throws DuplicateMappedEntryInfoException{
        DaoForClass annotation = dao.getClass().getAnnotation(DaoForClass.class);
        Class<T> clazz = ClassUtils.getEffectiveGenericType(dao.getClass(),CouchbaseDocumentDao.class,0);

        if(annotation!=null) {
            addDaoFor((Class<T>) annotation.value(), dao);
        }
        else if(clazz!=null) {
            addDaoFor(clazz, dao);
        }
        else{
            throw new NullPointerException("Annotation DaoForClass not defined for dao <"+dao.getClass().getName()+">");
        }
    }


    public List<CouchbaseDocumentDao> addDaoForEntities(List<String> entityPartialIds) throws DuplicateMappedEntryInfoException,ConfigPropertyValueNotFoundException{
        List<CouchbaseDocumentDao> result=new ArrayList<>(entityPartialIds.size());
        for(String entityPartialId:entityPartialIds){
            EntityModelId partialModelId = EntityModelId.buildPartial(entityPartialId);
            result.add(addDaoForEntityAndFlavor(partialModelId.getDomain(), partialModelId.getName(), null));
        }
        return result;
    }

    public List<CouchbaseDocumentDao> addDaoForEntitiesAndFlavor(List<String> entityPartialIds,String flavor) throws DuplicateMappedEntryInfoException,ConfigPropertyValueNotFoundException{
        List<CouchbaseDocumentDao> result=new ArrayList<>(entityPartialIds.size());
        for(String entityPartialId:entityPartialIds){
            EntityModelId partialModelId = EntityModelId.buildPartial(entityPartialId);
            result.add(addDaoForEntityAndFlavor(partialModelId.getDomain(), partialModelId.getName(), flavor));
        }
        return result;
    }

    public CouchbaseDocumentDao addDaoForEntity(String domain,String name) throws DuplicateMappedEntryInfoException,ConfigPropertyValueNotFoundException{
        return addDaoForEntityAndFlavor(domain, name, null);
    }

    public List<CouchbaseDocumentDao> addDaosForEffectiveDomainsEntity(Class<? extends CouchbaseDocument> clazz) throws DuplicateMappedEntryInfoException,ConfigPropertyValueNotFoundException{
        return addDaosForEffectiveDomainsEntityAndFlavor(clazz,null);
    }

    public Set<String> getEffectiveDomainsForClass(Class<? extends CouchbaseDocument> clazz){
        CouchbaseDocumentStructureReflection structureReflection = CouchbaseDocumentStructureReflection.getReflectionFromClass((Class)clazz);
        return entityDefinitionManager.getEffectiveDomains(EntityDef.build(structureReflection));
    }

    public List<CouchbaseDocumentDao> addDaosForEffectiveDomainsEntityAndFlavor(Class<? extends CouchbaseDocument> clazz,String flavor) throws DuplicateMappedEntryInfoException,ConfigPropertyValueNotFoundException{
        CouchbaseDocumentStructureReflection structureReflection = CouchbaseDocumentStructureReflection.getReflectionFromClass((Class)clazz);
        EntityDef rootEntity = EntityDef.build(structureReflection);
        DaoInfo daoInfo =DaoUtils.getDaoInfo(rootEntity.getModelId().getDomain(),rootEntity.getModelId().getName());
        Set<String> domains;
        if(daoInfo!=null && daoInfo.getParentDaoClassName()!=null){
            domains=new TreeSet<>();
            for(CouchbaseDocumentDao dao:attachedDaoList){
                if(dao.getClass().getName().equals(daoInfo.getParentDaoClassName())){
                    domains.add(dao.getDomain());
                }
            }
        }
        else {
            domains = getEffectiveDomainsForClass(clazz);
        }
        List<CouchbaseDocumentDao> daos = new ArrayList<>(domains.size());
        for (String domain : domains) {
            daos.add(addDaoForEntityAndFlavor(daoInfo, domain, rootEntity.getModelId().getName(), flavor));
        }
        return daos;
    }

    public CouchbaseDocumentDao addDaoForEntityAndFlavor(DaoInfo daoInfo,String domain,String name, String flavor) throws DuplicateMappedEntryInfoException,ConfigPropertyValueNotFoundException{
        try {
            Class<? extends CouchbaseDocumentDao> daoClass = (Class<? extends CouchbaseDocumentDao>) Thread.currentThread().getContextClassLoader().loadClass(daoInfo.getClassName());
            Class<? extends CouchbaseDocument> entityClass = (Class<? extends CouchbaseDocument>) Thread.currentThread().getContextClassLoader().loadClass(daoInfo.getEntityDef().getClassName());
            String bucketName = CouchbaseDaoConfigProperties.COUCHBASE_DAO_BUCKET_NAME_FOR_FLAVOR.getProperty(domain, name, flavor).getMandatoryValue("Cannot find entity class for domain {} / name {} / flavor {}", domain, name, flavor);
            Boolean readonly = CouchbaseDaoConfigProperties.COUCHBASE_DAO_READ_ONLY_FOR_FLAVOR.getProperty(domain, name, flavor).getValue();
            ICouchbaseBucket bucket = bucketFactory.getBucket(bucketName);
            CouchbaseDocumentDao dao = daoClass.newInstance();
            dao.setDomain(domain);
            if (readonly != null) {
                dao.isReadOnly(readonly);
            }
            dao.setClient(bucket);
            addDaoFor(entityClass, dao);
            return dao;
        }
        catch(ClassNotFoundException|IllegalAccessException|InstantiationException e) {
            throw new RuntimeException(e);//TODO improve errors
        }
    }

    public CouchbaseDocumentDao addDaoForEntityAndFlavor(String domain, String name, String flavor) throws DuplicateMappedEntryInfoException,ConfigPropertyValueNotFoundException{
        DaoInfo daoInfo =DaoUtils.getDaoInfo(domain, name);
        if(daoInfo==null){
            throw new RuntimeException("Cannot find dao for entity "+domain+"/"+name);
        }
        return addDaoForEntityAndFlavor(daoInfo,domain,name,flavor);
    }

    public <T extends CouchbaseDocument> CouchbaseDocumentDao<T> getDaoForClass(String domain,Class<T> entityClass) throws DaoNotFoundException{
        try {
            IDocumentClassMappingInfo info;
            if(domain==null){
                info=documentInfoMapper.getMappingFromClass(entityClass);
            }
            else{
                info=documentInfoMapper.getMappingFromClass(domain,entityClass);
            }
            @SuppressWarnings("unchecked")
            final CouchbaseDocumentDao<T> result = info.getAttachedObject(CouchbaseDocumentDao.class);
            if(result==null){
                throw new DaoNotFoundException(entityClass);
            }
            return result;
        }
        catch(MappingNotFoundException e){
            throw new DaoNotFoundException(entityClass);
        }
    }



    public CouchbaseDocumentDao getDaoForKey(String domain,String key) throws DaoNotFoundException {
        try {
            CouchbaseDocumentDao dao = documentInfoMapper.getMappingFromKey(domain,key).classMappingInfo().getAttachedObject(CouchbaseDocumentDao.class);
            if(dao==null){
                throw new DaoNotFoundException(domain,key,DaoNotFoundException.Type.DOC);
            }
            return dao;
        }
        catch(MappingNotFoundException e){
            throw new DaoNotFoundException(domain,key, DaoNotFoundException.Type.DOC);
        }
    }

    @Override
    public synchronized void init() {
        //nothing to do
    }

    @Override
    public synchronized void cleanup() {
        attachedDaoList.clear();
        counterDaoFactory.cleanup();
        viewDaoFactory.cleanup();
        documentInfoMapper.cleanup();
        if(daoRegistrar!=null){
            daoRegistrar.close();
        }
    }

    public static Builder builder(){
        return new Builder();
    }

    public EntityDefinitionManager getEntityDefinitionManager() {
        return entityDefinitionManager;
    }


    public static class Builder{
        private ICouchbaseBucketFactory couchbaseBucketFactory;
        private CouchbaseCounterDaoFactory.Builder counterDaoFactoryBuilder;
        private CouchbaseUniqueKeyDaoFactory.Builder uniqueKeyDaoFactoryBuilder;
        private CouchbaseViewDaoFactory.Builder viewDaoFactoryBuilder;
        private IDocumentInfoMapper documentInfoMapper;
        private CuratorFramework curatorFramework;
        private String daemonUid=null;
        private String webServerUid=null;

        public Builder(){
            couchbaseBucketFactory= null;
            documentInfoMapper = new DefaultDocumentMapperInfo();
            counterDaoFactoryBuilder = CouchbaseCounterDaoFactory.builder().withDocumentInfoMapper(documentInfoMapper);
            uniqueKeyDaoFactoryBuilder = CouchbaseUniqueKeyDaoFactory.builder().withDocumentInfoMapper(documentInfoMapper);
            viewDaoFactoryBuilder = CouchbaseViewDaoFactory.builder();
        }

        public Builder withBucketFactory(ICouchbaseBucketFactory bucketFactory){
            couchbaseBucketFactory=bucketFactory;
            return this;
        }

        public Builder withDocumentInfoMapper(IDocumentInfoMapper documentInfoMapper){
            this.documentInfoMapper = documentInfoMapper;
            counterDaoFactoryBuilder.withDocumentInfoMapper(documentInfoMapper);
            uniqueKeyDaoFactoryBuilder.withDocumentInfoMapper(documentInfoMapper);
            return this;
        }

        public Builder withCuratorFramework(CuratorFramework curatorFramework){
            this.curatorFramework=curatorFramework;
            return this;
        }

        public Builder withDaemonUid(String daemonUid) {
            this.daemonUid = daemonUid;
            return this;
        }

        public Builder withWebServerUid(String webServerUid) {
            this.webServerUid = webServerUid;
            return this;
        }

        public CouchbaseCounterDaoFactory.Builder getCounterDaoFactoryBuilder() {
            return counterDaoFactoryBuilder;
        }

        public CouchbaseUniqueKeyDaoFactory.Builder getUniqueKeyDaoFactoryBuilder() {
            return uniqueKeyDaoFactoryBuilder;
        }

        public CouchbaseViewDaoFactory.Builder getViewDaoFactoryBuilder() {
            return viewDaoFactoryBuilder;
        }

        public IDocumentInfoMapper getDocumentInfoMapper() {
            return documentInfoMapper;
        }

        public CouchbaseDocumentDaoFactory build(){
            return new CouchbaseDocumentDaoFactory(this);
        }
    }

}