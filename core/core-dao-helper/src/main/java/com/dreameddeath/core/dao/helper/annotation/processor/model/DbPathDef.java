/*
 * Copyright Christophe Jeunesse
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package com.dreameddeath.core.dao.helper.annotation.processor.model;

import com.dreameddeath.core.dao.helper.annotation.DaoEntity;
import com.dreameddeath.core.dao.helper.annotation.ParentEntity;
import com.dreameddeath.core.util.CouchbaseDocumentReflection;

import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.MirroredTypeException;

/**
 * Created by CEAJ8230 on 10/04/2015.
 */
public class DbPathDef {
    private String _basePath;
    private String _idFormat;
    private String _idPattern;
    private String _formatPrefix = "";
    private String _patternPrefix = "";

    public DbPathDef(CouchbaseDocumentReflection docReflection) {
        DaoEntity annot = docReflection.getClassInfo().getAnnotation(DaoEntity.class);
        _basePath = annot.dbPath();
        _idFormat = annot.idFormat();
        _idPattern = annot.idPattern();
        DbPathDef parentDbPath = null;
        ParentEntity parentAnnot = docReflection.getClassInfo().getAnnotation(ParentEntity.class);
        try {
            if (parentAnnot != null) {
                parentDbPath = new DbPathDef(CouchbaseDocumentReflection.getReflectionFromClass(parentAnnot.c()));
            }
        } catch (MirroredTypeException e) {
            parentDbPath = new DbPathDef(CouchbaseDocumentReflection.getReflectionFromTypeElement((TypeElement) ((DeclaredType) e.getTypeMirror()).asElement()));
        }
        if (parentDbPath != null) {
            _formatPrefix = "%s" + parentAnnot.separator();
            _patternPrefix = parentDbPath.getFullPattern() + parentAnnot.separator();
        }
    }


    public String getFullPattern() {
        return _patternPrefix + _basePath + _idPattern;
    }

    public String getFullFormat() {
        return _formatPrefix + _basePath + _idFormat;
    }

    public String getFormatPrefix() {
        return _formatPrefix;
    }

    public String getPatternPrefix() {
        return _patternPrefix;
    }

    public String getIdFormat() {
        return _idFormat;
    }

    public String getIdPattern() {
        return _idPattern;
    }

    public String getBasePath() {
        return _basePath;
    }
}
