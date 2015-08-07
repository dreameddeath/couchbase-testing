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
import com.dreameddeath.compile.tools.annotation.processor.reflection.AnnotationInfo;
import com.dreameddeath.compile.tools.annotation.processor.reflection.ClassInfo;
import com.dreameddeath.core.dao.annotation.DaoForClass;
import com.dreameddeath.core.helper.annotation.service.RestDao;
import com.dreameddeath.core.service.annotation.VersionStatus;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by Christophe Jeunesse on 19/04/2015.
 */
public class DaoRestServiceDef {
    private List<String> _imports = new ArrayList<>();
    private String _name;
    private String _domain;
    private String _version;
    private String _descr;
    private String _restRootPath;
    private String _restUIdName;
    private String _dbName;
    private VersionStatus _versionStatus;
    private DaoRestServiceDef _parentServiceDef=null;
    private AbstractClassInfo _rootClassInfo;

    public DaoRestServiceDef(ClassInfo daoInfo){
        RestDao daoRestAnnot = daoInfo.getAnnotation(RestDao.class);
        _name = daoRestAnnot.name();
        _domain = daoRestAnnot.domain();
        _version = daoRestAnnot.version();
        _descr = daoRestAnnot.descr();
        _versionStatus = daoRestAnnot.status();
        _dbName = daoRestAnnot.dbName();
        _restRootPath = daoRestAnnot.rootPath();
        _restUIdName = daoRestAnnot.uidName();
        AbstractClassInfo daoClassInfo = AnnotationInfo.getClassInfoFromAnnot(daoRestAnnot, RestDao::baseDao);
        //_imports.add(daoClassInfo.getImportName());  //Check if really needed
        AbstractClassInfo parentClassInfo = AnnotationInfo.getClassInfoFromAnnot(daoRestAnnot, RestDao::parentDao);
        if(parentClassInfo!=null){
            _parentServiceDef = new DaoRestServiceDef((ClassInfo) parentClassInfo);
        }

        DaoForClass daoClassAnnot = daoClassInfo.getAnnotation(DaoForClass.class);

        _rootClassInfo = AnnotationInfo.getClassInfoFromAnnot(daoClassAnnot, DaoForClass::value);
        _imports.add(_rootClassInfo.getImportName());

    }

    public List<String> getImports() {
        return Collections.unmodifiableList(_imports);
    }

    public String getRegisteringName(){
        return "dao#"+_domain+"#"+_name;
    }

    VersionStatus getStatus(){
        return _versionStatus;
    }
    public String getVersion() {
        return _version;
    }

    public String getName() {
        return _name;
    }

    public String getDbName() {
        return _dbName;
    }

    public String getClassSimpleName(){
        return _rootClassInfo.getSimpleName();
    }
    public List<String> getRootPathAttributeList() {
        if(_parentServiceDef!=null){
            return _parentServiceDef.getPathAttributeList();
        }
        else{
            return Collections.emptyList();
        }
    }

    public List<String> getPathAttributeList(){
        List<String> result = getRootPathAttributeList();
        result.add(getDefaultAttributeName());
        return result;
    }

    public String getRootRestPath(){
        return normalizePath(_domain+"/"+_version+"/"+ getInnerFullRestPath());
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
        return path.replaceAll("//","/");
    }
}
