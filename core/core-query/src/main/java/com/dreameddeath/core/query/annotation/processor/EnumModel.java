/*
 *
 *  * Copyright Christophe Jeunesse
 *  *
 *  *    Licensed under the Apache License, Version 2.0 (the "License");
 *  *    you may not use this file except in compliance with the License.
 *  *    You may obtain a copy of the License at
 *  *
 *  *      http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  *    Unless required by applicable law or agreed to in writing, software
 *  *    distributed under the License is distributed on an "AS IS" BASIS,
 *  *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  *    See the License for the specific language governing permissions and
 *  *    limitations under the License.
 *
 */

package com.dreameddeath.core.query.annotation.processor;

import com.dreameddeath.compile.tools.annotation.processor.reflection.ClassInfo;

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
    public boolean forRequest=false;
    public boolean forResponse=false;

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

    public boolean isForRequest() {
        return forRequest;
    }

    public boolean isForResponse() {
        return forResponse;
    }

    public List<String> getValues() {
        return values;
    }
}
