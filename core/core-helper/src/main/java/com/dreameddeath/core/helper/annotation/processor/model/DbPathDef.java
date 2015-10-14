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

import com.dreameddeath.core.helper.annotation.dao.DaoEntity;
import com.dreameddeath.core.helper.annotation.dao.ParentEntity;
import com.dreameddeath.core.model.util.CouchbaseDocumentReflection;

/**
 * Created by Christophe Jeunesse on 10/04/2015.
 */
public class DbPathDef {
    private String basePath;
    private String idFormat;
    private String idPattern;
    private String formatPrefix = "";
    private String patternPrefix = "";

    public DbPathDef(CouchbaseDocumentReflection docReflection) {
        DaoEntity annot = docReflection.getClassInfo().getAnnotation(DaoEntity.class);
        basePath = annot.dbPath();
        idFormat = annot.idFormat();
        idPattern = annot.idPattern();
        DbPathDef parentDbPath = null;
        ParentEntity parentAnnot = docReflection.getClassInfo().getAnnotation(ParentEntity.class);
        if(parentAnnot!=null) {
            CouchbaseDocumentReflection classInfo = CouchbaseDocumentReflection.getClassInfoFromAnnot(parentAnnot, ParentEntity::c);
            parentDbPath = new DbPathDef(classInfo);
        }

        if (parentDbPath != null) {
            formatPrefix = "%s" + parentAnnot.separator();
            patternPrefix = parentDbPath.getFullPattern() + parentAnnot.separator();
        }
    }


    public String getFullPattern() {
        return patternPrefix + basePath + idPattern;
    }

    public String getFullFormat() {
        return formatPrefix + basePath + idFormat;
    }

    public String getFormatPrefix() {
        return formatPrefix;
    }

    public String getPatternPrefix() {
        return patternPrefix;
    }

    public String getIdFormat() {
        return idFormat;
    }

    public String getIdPattern() {
        return idPattern;
    }

    public String getBasePath() {
        return basePath;
    }
}
