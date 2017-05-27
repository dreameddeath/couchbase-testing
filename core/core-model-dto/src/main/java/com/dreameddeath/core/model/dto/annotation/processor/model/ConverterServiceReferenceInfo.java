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

import com.squareup.javapoet.ClassName;

/**
 * Created by ceaj8230 on 06/03/2017.
 */
public class ConverterServiceReferenceInfo {
    private final String packageName;
    private final String shortName;
    private final String version;
    private final ClassName javaPoetClassName;


    public ConverterServiceReferenceInfo(String packageName, String shortName,String version) {
        this.packageName = packageName;
        this.shortName = shortName;
        this.version = version;
        String[] packageParts = packageName.split("\\$");
        StringBuilder newShotName = new StringBuilder();
        for(int pos=1;pos<packageParts.length;++pos){
            newShotName.append(packageParts[pos]);
            newShotName.append("$");
        }
        newShotName.append(shortName);
        this.javaPoetClassName = ClassName.get(packageParts[0],newShotName.toString());
    }

    public String getFullName(){
        return getPackageName()+"."+getShortName();
    }

    public String getPackageName(){
        return packageName;
    }

    public String getShortName(){
        return shortName;
    }

    public String getImportName(){
        return getFullName().replace("$",".");
    }

    public String getVersion() {
        return version;
    }

    public ClassName getClassName(){
        return javaPoetClassName;
    }
}
