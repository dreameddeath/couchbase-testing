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

package com.dreameddeath.couchbase.core.process.remote.annotation.processor.model;

import java.util.*;

/**
 * Created by Christophe Jeunesse on 04/03/2016.
 */
public class RestModel {
    private Map<String,Field> stringFieldMap = new HashMap<>();

    public String shortName;
    public String packageName;

    public String getShortName() {
        return shortName;
    }

    public String getPackageName() {
        return packageName;
    }

    public String getImportName(){
        return packageName+"."+shortName;
    }

    public List<String> getImports(){
        List<String> result = new ArrayList<>();
        for(Field field:stringFieldMap.values()){
            result.add(field.typeImport);
        }
        return result;
    }

    public Collection<Field> getFields(){
        return stringFieldMap.values();
    }

    public void addField(Field field){
        stringFieldMap.put(field.jobFieldName,field);
    }

    public static class Field{
        public String jobFieldName;
        public String variableName;
        public String jsonName;
        public String type;
        public String typeImport;

        public String buildGetter(String variableName){
            return variableName+"."+getGetterName()+"()";
        }

        public String getGetterName(){
            return "get"+variableName.substring(0,1).toUpperCase()+variableName.substring(1);
        }

        public String getSetterName(){
            return "set"+variableName.substring(0,1).toUpperCase()+variableName.substring(1);
        }

        public String getJobFieldName() {
            return jobFieldName;
        }

        public String getVariableName() {
            return variableName;
        }

        public String getJsonName() {
            return jsonName;
        }

        public String getType() {
            return type;
        }

        public String getTypeImport() {
            return typeImport;
        }
    }
}
