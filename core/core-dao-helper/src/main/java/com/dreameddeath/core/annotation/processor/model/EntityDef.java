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

package com.dreameddeath.core.annotation.processor.model;

import com.dreameddeath.core.annotation.dao.ParentEntity;
import com.dreameddeath.core.tools.annotation.processor.reflection.ClassInfo;
import com.dreameddeath.core.util.CouchbaseDocumentFieldReflection;
import com.dreameddeath.core.util.CouchbaseDocumentReflection;
import com.dreameddeath.core.util.CouchbaseDocumentStructureReflection;

/**
 * Created by CEAJ8230 on 10/04/2015.
 */
public class EntityDef {
    private CouchbaseDocumentReflection _docReflection;
    private String _parentKeyAccessor;

    public EntityDef(CouchbaseDocumentReflection docReflection) {
        _docReflection = docReflection;
        ParentEntity parentAnnot = docReflection.getClassInfo().getAnnotation(ParentEntity.class);
        if (parentAnnot != null) {
            String[] fieldNameParts = parentAnnot.keyPath().split("\\.");
            CouchbaseDocumentStructureReflection currStructure = docReflection.getStructure();
            _parentKeyAccessor = "";
            for (int partPos = 0; partPos < fieldNameParts.length; ++partPos) {
                CouchbaseDocumentFieldReflection field = currStructure.getFieldByPropertyName(fieldNameParts[partPos]);
                _parentKeyAccessor += "." + field.buildGetterCode();
                if (partPos + 1 < fieldNameParts.length) {
                    currStructure = CouchbaseDocumentStructureReflection.getReflectionFromClassInfo((ClassInfo) field.getEffectiveTypeInfo().getMainType());
                }
            }
        }
    }

    public String getSimpleName() {
        return _docReflection.getSimpleName();
    }

    public String getName() {
        return _docReflection.getName();
    }

    public String getDomain() {
        return _docReflection.getStructure().getStructDomain();
    }

    public String getParentKeyAccessor() {
        return _parentKeyAccessor;
    }

    public String getPackageName() {
        return _docReflection.getClassInfo().getPackageInfo().getName();
    }
}
