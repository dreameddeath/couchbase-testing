/*
 * 	Copyright Christophe Jeunesse
 *
 * 	Licensed under the Apache License, Version 2.0 (the "License");
 * 	you may not use this file except in compliance with the License.
 * 	You may obtain a copy of the License at
 *
 * 	http://www.apache.org/licenses/LICENSE-2.0
 *
 * 	Unless required by applicable law or agreed to in writing, software
 * 	distributed under the License is distributed on an "AS IS" BASIS,
 * 	WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * 	See the License for the specific language governing permissions and
 * 	limitations under the License.
 *
 */

package com.dreameddeath.core.dao.factory;

import com.dreameddeath.compile.tools.annotation.processor.reflection.AbstractClassInfo;
import com.dreameddeath.compile.tools.annotation.processor.reflection.ClassInfo;
import com.dreameddeath.core.couchbase.ICouchbaseBucket;
import com.dreameddeath.core.dao.annotation.DaoForClass;
import com.dreameddeath.core.dao.annotation.ParentDao;
import com.dreameddeath.core.dao.document.CouchbaseDocumentDao;
import com.dreameddeath.core.dao.model.utils.DaoInfo;
import com.dreameddeath.core.json.ObjectMapperFactory;
import com.dreameddeath.core.model.annotation.DocumentEntity;
import com.dreameddeath.core.model.document.CouchbaseDocument;
import com.dreameddeath.core.model.entity.model.EntityDef;
import com.dreameddeath.core.model.entity.model.EntityModelId;
import com.dreameddeath.core.model.exception.mapper.DuplicateMappedEntryInfoException;
import com.dreameddeath.core.model.util.CouchbaseDocumentReflection;
import com.dreameddeath.core.model.util.CouchbaseDocumentStructureReflection;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.io.InputStream;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Created by Christophe Jeunesse on 09/08/2015.
 */
public class DaoUtils {
    public static String ROOT_FILENAME_PER_CLASS = "META-INF/core-dao/perClassRegistering";
    public static String ROOT_FILENAME_PER_MODEL = "META-INF/core-dao/perModel";

    private static ObjectMapper mapper = ObjectMapperFactory.BASE_INSTANCE.getMapper();

    public static String getTargetDaoPerClassRegisteringFilename(DaoForClass annot){
        AbstractClassInfo classInfo = AbstractClassInfo.getClassInfoFromAnnot(annot,DaoForClass::value);
        return String.format("%s/%s", ROOT_FILENAME_PER_CLASS , classInfo.getFullName());
    }

    public static String getTargetDaoPerClassRegisteringFilename(Class<? extends CouchbaseDocument> clazz){
        AbstractClassInfo classInfo = AbstractClassInfo.getClassInfo(clazz);
        return getTargetDaoPerClassRegisteringFilename(classInfo);
    }


    public static String getTargetDaoPerClassRegisteringFilename(AbstractClassInfo classInfo){
        return String.format("%s/%s", ROOT_FILENAME_PER_CLASS , classInfo.getFullName());
    }


    public static String getTargetDaoPerModelRegisteringFilename(String domain,String name){
        return String.format("%s/%s/%s.json", ROOT_FILENAME_PER_MODEL, domain.toLowerCase(), name.toLowerCase());
    }

    public static String getTargetDaoPerModelRegisteringFilename(DaoForClass annot){
        AbstractClassInfo classInfo = AbstractClassInfo.getClassInfoFromAnnot(annot, DaoForClass::value);
        DocumentEntity docDefAnnot = classInfo.getAnnotation(DocumentEntity.class);
        if(docDefAnnot==null){
            throw new RuntimeException("Cannot find DocumentDef for class <"+classInfo.getFullName()+">");
        }

        EntityModelId modelId = EntityModelId.build(docDefAnnot, classInfo);
        return getTargetDaoPerModelRegisteringFilename(modelId.getDomain(), modelId.getName());
    }

    public static void writeDaoInfo(Writer writer,DaoForClass annot,ClassInfo daoClassInfo) throws IOException{
        AbstractClassInfo classInfo = AbstractClassInfo.getClassInfoFromAnnot(annot, DaoForClass::value);
        DocumentEntity docDefAnnot = classInfo.getAnnotation(DocumentEntity.class);
        if(docDefAnnot==null){
            throw new RuntimeException("Cannot find DocumentDef for class <"+classInfo.getFullName()+">");
        }

        DaoInfo daoInfo = new DaoInfo();
        daoInfo.setClassName(daoClassInfo.getName());
        daoInfo.setEntityDef(EntityDef.build(CouchbaseDocumentStructureReflection.getReflectionFromClassInfo((ClassInfo) classInfo)));

        ParentDao parentDao = daoClassInfo.getAnnotation(ParentDao.class);
        if(parentDao!=null){
            AbstractClassInfo parentDaoClassInfo = AbstractClassInfo.getClassInfoFromAnnot(parentDao, ParentDao::value);
           daoInfo.setParentDaoClassName(parentDaoClassInfo.getName());
        }

        mapper.writeValue(writer,daoInfo);
    }

    public static DaoInfo getDaoInfo(String domain, String name) {
        String filename = getTargetDaoPerModelRegisteringFilename(domain, name);
        try(InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream(filename)) {
            if (is != null) {
                return readDaoInfo(is, filename);
            } else {
                return null;
            }
        }
        catch (IOException e){
            throw new RuntimeException(e);
        }
    }

    public static DaoInfo readDaoInfo(InputStream is,String filename){
        try {
            return mapper.readValue(is, DaoInfo.class);
        }
        catch(Throwable e){
            throw new RuntimeException("Cannot read entity file <"+filename+">",e);
        }
    }

    public static DaoInfo getDaoInfo(CouchbaseDocumentReflection docReflexion){
        String filename = getTargetDaoPerClassRegisteringFilename(docReflexion.getClassInfo());
        try(InputStream is =Thread.currentThread().getContextClassLoader().getResourceAsStream(filename)){
            if(is!=null){
                return readDaoInfo(is,filename);
            }
            if(docReflexion.getClassInfo().getClass()!=null) {
                try (InputStream otherClassLoader = docReflexion.getClassInfo().getClass().getClassLoader().getResourceAsStream(filename)) {
                    if (otherClassLoader != null) {
                        return readDaoInfo(otherClassLoader, filename);
                    }
                }
            }

            if(docReflexion.getSuperclassReflection()!=null){
                return getDaoInfo(docReflexion.getSuperclassReflection());
            }
            else{
                return null;
            }
        }
        catch (IOException e){
            throw new RuntimeException(e);
        }
    }

    public static ClassInfo getDaoClassInfo(String domain,String name){
        DaoInfo daoInfo = getDaoInfo(domain, name);
        if(daoInfo!=null) {
            try {
                return (ClassInfo) AbstractClassInfo.getClassInfo(daoInfo.getClassName());
            }
            catch (ClassNotFoundException e) {
                throw new RuntimeException("Cannot load classInfo from className <" + daoInfo.getClassName() + "> for clazz <" + domain+"/"+name + ">", e);

            }
        }
        return null;
    }

    public static ClassInfo getDaoClassInfo(CouchbaseDocumentReflection docReflexion){
        DaoInfo daoInfo = getDaoInfo(docReflexion);
        if(daoInfo!=null) {
            try {
                return (ClassInfo) AbstractClassInfo.getClassInfo(daoInfo.getClassName());
            }
            catch (ClassNotFoundException e) {
                throw new RuntimeException("Cannot load classInfo from className <" + daoInfo.getClassName() + "> for clazz <" + docReflexion.getClassInfo().getCompiledFileName() + ">", e);
            }
        }
        return null;
    }

    public static <T extends CouchbaseDocument> List<CouchbaseDocumentDao<T>> buildAndAddDaosForDomains(CouchbaseDocumentDaoFactory factory, Class<? extends CouchbaseDocument> clazz,Class<? extends CouchbaseDocumentDao<T>> daoClazz,ICouchbaseBucket client) throws InstantiationException,IllegalAccessException,DuplicateMappedEntryInfoException{
        List<CouchbaseDocumentDao<T>> daos= new ArrayList<>();
        DaoInfo daoInfo = getDaoInfo(CouchbaseDocumentReflection.getReflectionFromClass(clazz));
        Set<String> domains;
        if(daoInfo!=null && daoInfo.getParentDaoClassName()!=null){
            domains = factory.getExistingDomainsForDaoClassName(daoInfo.getParentDaoClassName());
        }
        else{
            domains = factory.getEffectiveDomainsForClass(clazz);
        }
        for(String domain:domains){
            CouchbaseDocumentDao<T> dao = daoClazz.newInstance().setClient(client).setDomain(domain);
            daos.add(dao);
            factory.addDao(dao);
        }
        return daos;
    }
}
