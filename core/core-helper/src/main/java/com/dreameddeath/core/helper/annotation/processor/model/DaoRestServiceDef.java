/*
 *
 *  * Copyright Christophe Jeunesse
 *  *
 *  *    Licensed under the Apache License, Version 2.0 (the "License");
 *  *    you may not use this file except in compliance with the License.
 *  *    You may obtain a copy of the License at
 *  *
 *  *      http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  *    Unless required by applicable law or agreed to in writing, software
 *  *    distributed under the License is distributed on an "AS IS" BASIS,
 *  *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  *    See the License for the specific language governing permissions and
 *  *    limitations under the License.
 *
 */

package com.dreameddeath.core.helper.annotation.processor.model;

import com.dreameddeath.compile.tools.annotation.processor.reflection.AbstractClassInfo;
import com.dreameddeath.compile.tools.annotation.processor.reflection.ClassInfo;
import com.dreameddeath.core.dao.annotation.DaoForClass;
import com.dreameddeath.core.dao.factory.DaoUtils;
import com.dreameddeath.core.helper.annotation.service.RestDao;
import com.dreameddeath.core.model.util.CouchbaseDocumentReflection;
import com.dreameddeath.core.service.annotation.VersionStatus;
import com.google.common.base.Preconditions;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Created by Christophe Jeunesse on 19/04/2015.
 */
public class DaoRestServiceDef {
    private List<String> imports = new ArrayList<>();
    private String name;
    private String domain;
    private String version;
    private String descr;
    private String dbPath;
    private String restRootPath;
    private String restUIdName;
    private VersionStatus versionStatus;
    private DaoRestServiceDef parentServiceDef=null;
    private CouchbaseDocumentReflection rootClassInfo;
    private String targetPackage;
    private String targetReadName;
    private String targetWriteName;

    public DaoRestServiceDef(ClassInfo daoInfo,Map<ClassInfo,DaoDef> daoMap){
        RestDao daoRestAnnot = daoInfo.getAnnotation(RestDao.class);
        targetPackage = daoInfo.getPackageInfo().getName().replaceAll("\\bdao\\b", "service");
        targetReadName = daoInfo.getSimpleName().replaceAll("Dao$", "ReadRestService");
        targetWriteName = daoInfo.getSimpleName().replaceAll("Dao$", "WriteRestService");
        name = daoRestAnnot.name();
        domain = daoRestAnnot.domain();
        version = daoRestAnnot.version();
        descr = daoRestAnnot.descr();
        dbPath = daoRestAnnot.dbPath();
        versionStatus = daoRestAnnot.status();
        restRootPath = daoRestAnnot.rootPath();
        restUIdName = daoRestAnnot.uidName();

        DaoForClass daoClassAnnot = daoInfo.getAnnotation(DaoForClass.class);
        rootClassInfo = CouchbaseDocumentReflection.getClassInfoFromAnnot(daoClassAnnot,DaoForClass::value);
        Preconditions.checkNotNull(rootClassInfo,"Cannot get orig document classInfo from class {}",daoClassAnnot.getClass());
        imports.add(rootClassInfo.getClassInfo().getImportName());

        //AbstractClassInfo parentClassInfo = AnnotationInfo.getClassInfoFromAnnot(daoRestAnnot, RestDao::parentDao);
        if(! daoRestAnnot.parentClassName().equals("")){
            CouchbaseDocumentReflection parentEntityClassInfo=null;
            try {
                parentEntityClassInfo = CouchbaseDocumentReflection.getClassInfo(daoRestAnnot.parentClassName());
                Preconditions.checkNotNull(parentEntityClassInfo,"Cannot find parent classInfo from class {}",daoRestAnnot.getClass());
            }
            catch(ClassNotFoundException e){
                throw new RuntimeException("Cannot find parent entity class "+daoRestAnnot.parentClassName()+" from class "+rootClassInfo.getName());
            }


            if(daoMap.containsKey(parentEntityClassInfo.getClassInfo())){
                String className = daoMap.get(parentEntityClassInfo.getClassInfo()).getName();
                try {
                    parentServiceDef = new DaoRestServiceDef((ClassInfo)AbstractClassInfo.getClassInfo(className),daoMap);
                }
                catch(ClassNotFoundException e){
                    throw new RuntimeException("Cannot find dao of parent class "+parentEntityClassInfo.getName()+" for class "+rootClassInfo.getName());
                }
            }
            else{
                 ClassInfo parentClassDao = DaoUtils.getDaoClassInfo(parentEntityClassInfo);
                if(parentClassDao!=null) {
                    parentServiceDef = new DaoRestServiceDef(parentClassDao, daoMap);
                }
            }
        }
    }

    public List<String> getImports() {
        return Collections.unmodifiableList(imports);
    }

    public String getRegisteringName(){
        return "dao#"+domain+"#"+name;
    }

    public String getStatus(){
        return versionStatus.getClass().getEnumConstants()[versionStatus.ordinal()].name() ;
    }
    public String getVersion() {
        return version;
    }

    public String getName() {
        return name;
    }

    public String getDomain() {
        return domain;
    }

    public String getClassSimpleName(){
        return rootClassInfo.getSimpleName();
    }


    public List<AttributInfo> getRootPathAttributeInfoList() {
        if(parentServiceDef!=null){
            return parentServiceDef.getPathAttributeInfoList();
        }
        else{
            return new ArrayList<>();
        }
    }

    public List<AttributInfo> getPathAttributeInfoList(){
        List<AttributInfo> result = getRootPathAttributeInfoList();

        result.add(new AttributInfo(getDefaultAttributeName(),"String","Id of Element "+name));
        return result;
    }

    public String getRootRestPath(){
        return normalizePath(domain+"/"+version+"/"+ getInnerRootRestPath());
    }

    private String getInnerFullRestPath(){
        return normalizePath(getInnerRootRestPath()+"/"+getDefaultSubPath());
    }

    private String getInnerRootRestPath(){
        String result;
        if(parentServiceDef!=null){
            result="/"+parentServiceDef.getInnerFullRestPath();
        }
        else{
            result = "";
        }
        result+="/"+restRootPath;
        return normalizePath(result);
    }

    public String getDefaultSubPath(){
        return normalizePath("{"+ restUIdName +"}");
    }

    public String getDefaultAttributeName(){
        return restUIdName;
    }

    private String normalizePath(String path){
        return path.replaceAll("/{2,}","/");
    }

    public String getReadFullName(){
        return targetPackage+"."+getTargetReadName();
    }

    public String getWriteFullName(){
        return targetPackage+"."+getTargetWriteName();
    }

    public String getTargetReadName(){
        return targetReadName;
    }
    public String getTargetWriteName(){
        return targetWriteName;
    }

    public String getPackage(){
        return targetPackage;
    }


    public String getDbName(){return name;}
    public String getDbPath(){
        return dbPath;
    }


    public String getRootDbKeyPattern(){
        return (parentServiceDef!=null)?parentServiceDef.getFullDbKeyPattern():"";
    }
    public String getFullDbKeyPattern(){
        String result =getRootDbKeyPattern();
        if(!result.equals("")){
            result+="/";
        }
        result+=getDbPath()+"/%s";
        return result.replaceAll("/{2,}","/");
    }

    public static class AttributInfo{
        private String type;
        private String name;
        private String descr;

        public AttributInfo(String name,String type,String descr){
            this.name = name;
            this.type = type;
            this.descr = descr;
        }

        public String getType() {
            return type;
        }

        public String getName() {
            return name;
        }

        public String getDescr() {
            return descr;
        }
    }
}
