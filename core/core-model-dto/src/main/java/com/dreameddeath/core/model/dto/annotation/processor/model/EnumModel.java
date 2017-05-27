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

package com.dreameddeath.core.model.dto.annotation.processor.model;

import com.dreameddeath.compile.tools.annotation.processor.reflection.ClassInfo;
import com.dreameddeath.compile.tools.annotation.processor.reflection.FieldInfo;
import com.dreameddeath.core.model.dto.annotation.processor.ConverterGeneratorContext;
import com.squareup.javapoet.ClassName;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Christophe Jeunesse on 13/05/2016.
 */
public class EnumModel {
    public List<String> values=new ArrayList<>();
    public String shortName;
    public String packageName;
    public ClassInfo origClassInfo;
    public boolean forInput =false;
    public boolean forOutput =false;

    public String getImportName(){
        return packageName+"."+shortName;
    }

    public String getShortName() {
        return shortName;
    }

    public String getPackageName(){
        return packageName;
    }

    public String getOrigClassSimpleName(){ return origClassInfo.getSimpleName();}

    public boolean isForInput() {
        return forInput;
    }

    public boolean isForOutput() {
        return forOutput;
    }

    public List<String> getValues() {
        return values;
    }

    public static EnumModel buildEnum(ConverterGeneratorContext context,ClassInfo classInfo, String packageName) {
        EnumModel result = new EnumModel();
        result.shortName = classInfo.getSimpleName().replaceAll("\\$","")+"Published";
        result.packageName = packageName;
        result.origClassInfo = classInfo;

        context.putEnumModel(classInfo.getFullName(),result);

        for(FieldInfo fieldInfo:classInfo.getDeclaredFields()){
            result.values.add(fieldInfo.getName());
        }
        return result;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        EnumModel enumModel = (EnumModel) o;

        if (!shortName.equals(enumModel.shortName)) return false;
        return packageName.equals(enumModel.packageName);
    }

    @Override
    public int hashCode() {
        int result = shortName.hashCode();
        result = 31 * result + packageName.hashCode();
        return result;
    }

    public ClassName getClassName() {
        return ClassName.get(getPackageName(),getShortName());
    }
}
