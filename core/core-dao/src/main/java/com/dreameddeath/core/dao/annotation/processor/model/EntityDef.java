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

package com.dreameddeath.core.dao.annotation.processor.model;

import com.dreameddeath.compile.tools.annotation.processor.reflection.ClassInfo;
import com.dreameddeath.core.dao.annotation.dao.ParentEntity;
import com.dreameddeath.core.java.utils.StringUtils;
import com.dreameddeath.core.model.util.CouchbaseDocumentFieldReflection;
import com.dreameddeath.core.model.util.CouchbaseDocumentReflection;
import com.dreameddeath.core.model.util.CouchbaseDocumentStructureReflection;
import com.google.common.base.Preconditions;

/**
 * Created by Christophe Jeunesse on 10/04/2015.
 */
public class EntityDef {
    private CouchbaseDocumentReflection docReflection;
    private String parentKeyAccessor;
    private String parentKeyPath;
    private String parentEntityClassName ="";

    public EntityDef(CouchbaseDocumentReflection docReflection) {
        this.docReflection = docReflection;
        ParentEntity parentAnnot = docReflection.getClassInfo().getAnnotation(ParentEntity.class);

        if((parentAnnot==null)|| StringUtils.isEmpty(parentAnnot.keyPath())){
            parentKeyPath = "null";
        }
        else{
            parentKeyPath = "doc."+parentAnnot.keyPath();
        }

        if (parentAnnot != null) {

            CouchbaseDocumentReflection classInfo = CouchbaseDocumentReflection.getClassInfoFromAnnot(parentAnnot, ParentEntity::c);
            Preconditions.checkNotNull(classInfo,"Cannot get parent class info from class {}",parentAnnot);
            parentEntityClassName = classInfo.getName();

            String[] fieldNameParts = parentAnnot.keyPath().split("\\.");

            CouchbaseDocumentStructureReflection currStructure = docReflection.getStructure();
            parentKeyAccessor = "";
            for (int partPos = 0; partPos < fieldNameParts.length; ++partPos) {
                CouchbaseDocumentFieldReflection field = currStructure.getFieldByPropertyName(fieldNameParts[partPos]);
                parentKeyAccessor += "." + field.buildGetterCode();
                if(partPos + 1 < fieldNameParts.length){
                    currStructure = CouchbaseDocumentStructureReflection.getReflectionFromClassInfo((ClassInfo) field.getEffectiveTypeInfo().getMainType());
                }
            }
        }
    }

    public String getSimpleName() {
        return docReflection.getSimpleName();
    }

    public String getName() {
        return docReflection.getName();
    }

    public String getDomain() {
        return docReflection.getStructure().getStructDomain();
    }

    public String getDbName(){return docReflection.getStructure().getStructName();}

    public String getVersion(){return docReflection.getStructure().getStructVersion();}

    public String getParentKeyAccessor() {
        return parentKeyAccessor;
    }

    public String getParentKeyPath() {
        return parentKeyPath;
    }

    public String getPackageName() {
        return docReflection.getClassInfo().getPackageInfo().getName();
    }

    public String getParentEntityClassName() {
        return parentEntityClassName;
    }

    public CouchbaseDocumentReflection getDocReflection() {
        return docReflection;
    }
}
