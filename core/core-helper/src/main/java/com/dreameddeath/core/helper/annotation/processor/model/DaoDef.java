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
import com.dreameddeath.compile.tools.annotation.processor.reflection.FieldInfo;
import com.dreameddeath.core.business.dao.BusinessCouchbaseDocumentDaoWithUID;
import com.dreameddeath.core.dao.document.CouchbaseDocumentWithKeyPatternDao;
import com.dreameddeath.core.helper.annotation.dao.DaoEntity;
import com.dreameddeath.core.helper.annotation.dao.UidDef;
import com.dreameddeath.core.model.util.CouchbaseDocumentFieldReflection;
import com.dreameddeath.core.model.util.CouchbaseDocumentReflection;
import com.dreameddeath.core.model.util.CouchbaseDocumentStructureReflection;

import java.util.UUID;

/**
 * Created by Christophe Jeunesse on 10/04/2015.
 */
public class DaoDef {
    private AbstractClassInfo _baseDaoClassInfo;
    private String _simpleName;
    private String _packageName;
    private Type _type;
    private UidType _uidType;
    private boolean _isUidPureField;
    private String _uidSetterPattern;
    private String _uidGetterPattern;
    private boolean _generateRestLayer;

    public DaoDef(CouchbaseDocumentReflection docReflection) {

        _simpleName = docReflection.getSimpleName().replaceAll("\\$", "") + "Dao";
        _packageName = docReflection.getClassInfo().getPackageInfo().getName().replaceAll("\\bmodel\\b", "dao");
        DaoEntity daoEntityAnnot = docReflection.getClassInfo().getAnnotation(DaoEntity.class);
        _baseDaoClassInfo = AbstractClassInfo.getClassInfoFromAnnot(daoEntityAnnot, DaoEntity::baseDao);
        _generateRestLayer = daoEntityAnnot.rest();

        if (_baseDaoClassInfo.isInstanceOf(CouchbaseDocumentWithKeyPatternDao.class)) {
            if (_baseDaoClassInfo.isInstanceOf(BusinessCouchbaseDocumentDaoWithUID.class)) {
                _type = Type.WITH_UID;
                UidDef uidDef = docReflection.getClassInfo().getAnnotation(UidDef.class);
                if (uidDef != null) {
                    String[] fieldNameParts = uidDef.fieldName().split("\\.");
                    _uidGetterPattern = "";
                    _uidSetterPattern = "";
                    CouchbaseDocumentStructureReflection currStructure = docReflection.getStructure();
                    for (int partPos = 0; partPos < fieldNameParts.length; ++partPos) {
                        CouchbaseDocumentFieldReflection field = currStructure.getFieldByPropertyName(fieldNameParts[partPos]);
                        //Last element
                        if (partPos + 1 == fieldNameParts.length) {
                            _isUidPureField = field.getSetter() instanceof FieldInfo;
                            _uidSetterPattern += _uidGetterPattern + "." + field.getSetterName();

                            if (UUID.class.isAssignableFrom(field.getEffectiveTypeClass())) {
                                _uidType = UidType.UUID;
                            } else if (Long.class.isAssignableFrom(field.getEffectiveTypeClass())) {
                                _uidType = UidType.LONG;
                            } else if (String.class.isAssignableFrom(field.getEffectiveTypeClass())) {
                                _uidType = UidType.STRING;
                            } else {
                                //TODO throw an error
                            }

                        }
                        _uidGetterPattern += "." + field.buildGetterCode();
                        if (partPos + 1 < fieldNameParts.length) {
                            currStructure = CouchbaseDocumentStructureReflection.getReflectionFromClassInfo((ClassInfo) field.getEffectiveTypeInfo().getMainType());
                        }
                    }
                } else {
                    //TODO throw error
                }
            } else {
                _type = Type.WITH_PATTERN;
            }
        } else {
            _type = Type.BASE;
        }

    }

    public String getSimpleName() {
        return _simpleName;
    }

    public String getPackageName() {
        return _packageName;
    }

    public String getName() {
        return _packageName + "." + _simpleName;
    }

    public String getFilename() {
        return _packageName.replaceAll("\\.", "/") + "/" + _simpleName + ".java";
    }

    public String getBaseSimpleName() {
        return _baseDaoClassInfo.getSimpleName();
    }

    public String getBaseName() {
        return _baseDaoClassInfo.getName();
    }

    public String getBaseImportName() {
        return _baseDaoClassInfo.getImportName();
    }

    public Type getType() {
        return _type;
    }

    public boolean isUidTypeLong() {
        return UidType.LONG.equals(_type);
    }

    public UidType getUidType() {
        return _uidType;
    }

    public String getUidSetterPattern() {
        return _uidSetterPattern;
    }

    public String buildUidSetter(String data) {
        if (_isUidPureField) return getUidSetterPattern() + "=" + data;
        else return getUidSetterPattern() + "(" + data + ")";
    }


    public enum Type {
        BASE(false, false),
        WITH_PATTERN(true, false),
        WITH_UID(true, true);

        private boolean _hasPattern;
        private boolean _hasUid;

        Type(boolean pattern, boolean uid) {
            _hasPattern = pattern;
            _hasUid = uid;
        }

        public boolean hasPattern() {
            return _hasPattern;
        }

        public boolean hasUid() {
            return _hasUid;
        }
    }

    public boolean needGenerateRestLayer() {
        return _generateRestLayer;
    }

    public enum UidType {
        LONG,
        UUID,
        STRING
    }
}
