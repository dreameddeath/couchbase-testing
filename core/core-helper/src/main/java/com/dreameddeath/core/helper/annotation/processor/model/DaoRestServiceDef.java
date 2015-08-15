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

package com.dreameddeath.core.helper.annotation.processor.model;

import com.dreameddeath.compile.tools.annotation.processor.reflection.AbstractClassInfo;
import com.dreameddeath.compile.tools.annotation.processor.reflection.ClassInfo;
import com.dreameddeath.core.dao.annotation.DaoForClass;
import com.dreameddeath.core.dao.utils.DaoUtils;
import com.dreameddeath.core.helper.annotation.service.RestDao;
import com.dreameddeath.core.model.util.CouchbaseDocumentReflection;
import com.dreameddeath.core.service.annotation.VersionStatus;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Created by Christophe Jeunesse on 19/04/2015.
 */
public class DaoRestServiceDef {
    private List<String> _imports = new ArrayList<>();
    private String _name;
    private String _domain;
    private String _version;
    private String _descr;
    private String _dbPath;
    private String _restRootPath;
    private String _restUIdName;
    private VersionStatus _versionStatus;
    private DaoRestServiceDef _parentServiceDef=null;
    private CouchbaseDocumentReflection _rootClassInfo;
    private String _targetPackage;
    private String _targetReadName;
    private String _targetWriteName;

    public DaoRestServiceDef(ClassInfo daoInfo,Map<ClassInfo,DaoDef> daoMap){
        RestDao daoRestAnnot = daoInfo.getAnnotation(RestDao.class);
        _targetPackage = daoInfo.getPackageInfo().getName().replaceAll("\\bdao\\b", "service");
        _targetReadName = daoInfo.getSimpleName().replaceAll("Dao$", "ReadRestService");
        _targetWriteName = daoInfo.getSimpleName().replaceAll("Dao$", "WriteRestService");
        _name = daoRestAnnot.name();
        _domain = daoRestAnnot.domain();
        _version = daoRestAnnot.version();
        _descr = daoRestAnnot.descr();
        _dbPath = daoRestAnnot.dbPath();
        _versionStatus = daoRestAnnot.status();
        _restRootPath = daoRestAnnot.rootPath();
        _restUIdName = daoRestAnnot.uidName();

        DaoForClass daoClassAnnot = daoInfo.getAnnotation(DaoForClass.class);
        _rootClassInfo = CouchbaseDocumentReflection.getClassInfoFromAnnot(daoClassAnnot,DaoForClass::value);
        _imports.add(_rootClassInfo.getClassInfo().getImportName());

        //AbstractClassInfo parentClassInfo = AnnotationInfo.getClassInfoFromAnnot(daoRestAnnot, RestDao::parentDao);
        if(! daoRestAnnot.parentClassName().equals("")){
            CouchbaseDocumentReflection parentEntityClassInfo=null;
            try {
                parentEntityClassInfo = CouchbaseDocumentReflection.getClassInfo(daoRestAnnot.parentClassName());
            }
            catch(ClassNotFoundException e){
                throw new RuntimeException("Cannot find parent entity class "+daoRestAnnot.parentClassName()+" from class "+_rootClassInfo.getName());
            }


            if(daoMap.containsKey(parentEntityClassInfo.getClassInfo())){
                String className = daoMap.get(parentEntityClassInfo.getClassInfo()).getName();
                try {
                    _parentServiceDef = new DaoRestServiceDef((ClassInfo)AbstractClassInfo.getClassInfo(className),daoMap);
                }
                catch(ClassNotFoundException e){
                    throw new RuntimeException("Cannot find dao of parent class "+parentEntityClassInfo.getName()+" for class "+_rootClassInfo.getName());
                }
            }
            else{
                 ClassInfo parentClassDao = DaoUtils.getDaoFromClass(parentEntityClassInfo);
                if(parentClassDao!=null) {
                    _parentServiceDef = new DaoRestServiceDef(parentClassDao, daoMap);
                }
            }
        }
    }

    public List<String> getImports() {
        return Collections.unmodifiableList(_imports);
    }

    public String getRegisteringName(){
        return "dao#"+_domain+"#"+_name;
    }

    public String getStatus(){
        return _versionStatus.getClass().getEnumConstants()[_versionStatus.ordinal()].name() ;
    }
    public String getVersion() {
        return _version;
    }

    public String getName() {
        return _name;
    }


    public String getClassSimpleName(){
        return _rootClassInfo.getSimpleName();
    }


    public List<AttributInfo> getRootPathAttributeInfoList() {
        if(_parentServiceDef!=null){
            return _parentServiceDef.getPathAttributeInfoList();
        }
        else{
            return new ArrayList<>();
        }
    }

    public List<AttributInfo> getPathAttributeInfoList(){
        List<AttributInfo> result = getRootPathAttributeInfoList();

        result.add(new AttributInfo(getDefaultAttributeName(),"String","Id of Element "+_name));
        return result;
    }

    public String getRootRestPath(){
        return normalizePath(_domain+"/"+_version+"/"+ getInnerRootRestPath());
    }

    private String getInnerFullRestPath(){
        return normalizePath(getInnerRootRestPath()+"/"+getDefaultSubPath());
    }

    private String getInnerRootRestPath(){
        String result;
        if(_parentServiceDef!=null){
            result="/"+_parentServiceDef.getInnerFullRestPath();
        }
        else{
            result = "";
        }
        result+="/"+_restRootPath;
        return normalizePath(result);
    }

    public String getDefaultSubPath(){
        return normalizePath("{"+ _restUIdName +"}");
    }

    public String getDefaultAttributeName(){
        return _restUIdName;
    }

    private String normalizePath(String path){
        return path.replaceAll("/{2,}","/");
    }

    public String getReadFullName(){
        return _targetPackage+"."+getTargetReadName();
    }

    public String getWriteFullName(){
        return _targetPackage+"."+getTargetWriteName();
    }

    public String getTargetReadName(){
        return _targetReadName;
    }
    public String getTargetWriteName(){
        return _targetWriteName;
    }

    public String getPackage(){
        return _targetPackage;
    }


    public String getDbName(){return _name;}
    public String getDbPath(){
        return _dbPath;
    }


    public String getRootDbKeyPattern(){
        return (_parentServiceDef!=null)?_parentServiceDef.getFullDbKeyPattern():"";
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
        private String _type;
        private String _name;
        private String _descr;

        public AttributInfo(String name,String type,String descr){
            _name = name;
            _type = type;
            _descr = descr;
        }

        public String getType() {
            return _type;
        }

        public String getName() {
            return _name;
        }

        public String getDescr() {
            return _descr;
        }
    }
}
