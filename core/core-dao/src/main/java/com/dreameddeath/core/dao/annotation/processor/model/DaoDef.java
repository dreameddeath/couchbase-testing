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

package com.dreameddeath.core.dao.annotation.processor.model;

import com.dreameddeath.compile.tools.annotation.processor.reflection.AbstractClassInfo;
import com.dreameddeath.compile.tools.annotation.processor.reflection.ClassInfo;
import com.dreameddeath.compile.tools.annotation.processor.reflection.FieldInfo;
import com.dreameddeath.core.dao.annotation.dao.Counter;
import com.dreameddeath.core.dao.annotation.dao.DaoEntity;
import com.dreameddeath.core.dao.annotation.dao.UidDef;
import com.dreameddeath.core.dao.document.IDaoForDocumentWithUID;
import com.dreameddeath.core.dao.document.IDaoWithKeyPattern;
import com.dreameddeath.core.model.util.CouchbaseDocumentFieldReflection;
import com.dreameddeath.core.model.util.CouchbaseDocumentReflection;
import com.dreameddeath.core.model.util.CouchbaseDocumentStructureReflection;
import com.google.common.base.Preconditions;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

/**
 * Created by Christophe Jeunesse on 10/04/2015.
 */
public class DaoDef {
    private final AbstractClassInfo baseDaoClassInfo;
    private final String simpleName;
    private final String packageName;
    private final Type type;
    private UidType uidType;
    private boolean isUidPureField;
    private String uidSetterPattern;
    private String uidGetterPattern;
    private boolean isSharedKeyAccrossDomain;
    private final List<String> pluginImportNames = new ArrayList<>();
    private List<String> pluginAnnotationStrings = new ArrayList<>();

    public DaoDef(CouchbaseDocumentReflection docReflection) {
        simpleName = docReflection.getSimpleName().replaceAll("\\$", "") + "Dao";
        packageName = docReflection.getClassInfo().getPackageInfo().getName().replaceAll("\\bmodel\\b", "dao");
        DaoEntity daoEntityAnnot = docReflection.getClassInfo().getAnnotation(DaoEntity.class);
        baseDaoClassInfo = AbstractClassInfo.getClassInfoFromAnnot(daoEntityAnnot, DaoEntity::baseDao);

        if (baseDaoClassInfo.isInstanceOf(IDaoWithKeyPattern.class)) {
            isSharedKeyAccrossDomain =daoEntityAnnot.sharedKeyAccrossDomain();
            if (baseDaoClassInfo.isInstanceOf(IDaoForDocumentWithUID.class)) {
                type = Type.WITH_UID;
                UidDef uidDef = docReflection.getClassInfo().getAnnotation(UidDef.class);
                if (uidDef != null) {
                    String[] fieldNameParts = uidDef.fieldName().split("\\.");
                    uidGetterPattern = "";
                    uidSetterPattern = "";
                    CouchbaseDocumentStructureReflection currStructure = docReflection.getStructure();
                    for (int partPos = 0; partPos < fieldNameParts.length; ++partPos) {
                        CouchbaseDocumentFieldReflection field = currStructure.getFieldByPropertyName(fieldNameParts[partPos]);
                        //Last element
                        if (partPos + 1 == fieldNameParts.length) {
                            isUidPureField = field.getSetter() instanceof FieldInfo;
                            uidSetterPattern += uidGetterPattern + "." + field.getSetterName();

                            if (UUID.class.isAssignableFrom(field.getEffectiveTypeClass())) {
                                uidType = UidType.UUID;
                            } else if (Long.class.isAssignableFrom(field.getEffectiveTypeClass())) {
                                uidType = UidType.LONG;
                            } else if (String.class.isAssignableFrom(field.getEffectiveTypeClass())) {
                                uidType = UidType.STRING;
                            } else {
                                //TODO throw an error
                            }

                        }
                        uidGetterPattern += "." + field.buildGetterCode();
                        if (partPos + 1 < fieldNameParts.length) {
                            currStructure = CouchbaseDocumentStructureReflection.getReflectionFromClassInfo((ClassInfo) field.getEffectiveTypeInfo().getMainType());
                        }
                    }
                } else {
                    throw new IllegalStateException("The dao def "+simpleName+" of type "+type+ " must have the annotation "+UidDef.class.getName());
                    //TODO throw error
                }
            } else {
                type = Type.WITH_PATTERN;
                Counter keyGenCounter = null;
                Counter[] counters = docReflection.getClassInfo().getAnnotationByType(Counter.class);
                if(counters!=null){
                    for(Counter counter : counters){
                        if(counter.isKeyGen()){
                            keyGenCounter = counter;
                            break;
                        }
                    }
                }

                Preconditions.checkArgument(keyGenCounter!=null,"The entity %s with pattern gen dao must have a key counter",docReflection.getClassInfo().getFullName());
            }
        } else {
            throw new IllegalStateException("Dao Type "+baseDaoClassInfo.getName()+" for dao "+simpleName+" not managed yet");
        }

    }

    public String getSimpleName() {
        return simpleName;
    }

    public String getPackageName() {
        return packageName;
    }

    public String getName() {
        return packageName + "." + simpleName;
    }

    public String getFilename() {
        return packageName.replaceAll("\\.", "/") + "/" + simpleName + ".java";
    }

    public String getBaseSimpleName() {
        return baseDaoClassInfo.getSimpleName();
    }

    public String getBaseName() {
        return baseDaoClassInfo.getName();
    }

    public String getBaseImportName() {
        return baseDaoClassInfo.getImportName();
    }

    public Type getType() {
        return type;
    }

    public boolean isUidTypeLong() {
        return UidType.LONG.equals(uidType);
    }

    public UidType getUidType() {
        return uidType;
    }

    public String getUidSetterPattern() {
        return uidSetterPattern;
    }

    public String buildUidSetter(String data) {
        if (isUidPureField) return getUidSetterPattern() + "=" + data;
        else return getUidSetterPattern() + "(" + data + ")";
    }

    public boolean isSharedKeyAccrossDomain() {
        return isSharedKeyAccrossDomain;
    }

    public List<String> getPluginAnnotationStrings() {
        return Collections.unmodifiableList(pluginAnnotationStrings);
    }

    public void addPluginAnnotationStrings(String pluginAnnotationString) {
        this.pluginAnnotationStrings.add(pluginAnnotationString);
    }

    public List<String> getPluginsImportNames() {
        return Collections.unmodifiableList(pluginImportNames);
    }

    public void addPluginImportName(String importName){
        pluginImportNames.add(importName);
    }



    public enum Type {
        BASE(false, false),
        WITH_PATTERN(true, false),
        WITH_UID(true, true);

        private boolean hasPattern;
        private boolean hasUid;

        Type(boolean pattern, boolean uid) {
            hasPattern = pattern;
            hasUid = uid;
        }

        public boolean hasPattern() {
            return hasPattern;
        }

        public boolean hasUid() {
            return hasUid;
        }
    }

    public enum UidType {
        LONG,
        UUID,
        STRING
    }
}
