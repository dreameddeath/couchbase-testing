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

package com.dreameddeath.core.helper.annotation.processor.model;

import com.dreameddeath.core.helper.annotation.dao.Counter;
import com.dreameddeath.core.model.util.CouchbaseDocumentReflection;

/**
 * Created by Christophe Jeunesse on 10/04/2015.
 */
public class CounterDef {
    private String name;
    private String dbName;
    private String pattern;
    private DbPathDef dbPathDef;
    private boolean isKeyGen;
    private long defaultValue;
    private long modulus;

    public CounterDef(CouchbaseDocumentReflection docReflection, Counter annot, DbPathDef pathDef) {
        name = annot.name();
        dbName = annot.name();
        isKeyGen = annot.isKeyGen();
        defaultValue = annot.defaultValue();
        modulus = annot.modulus();
        dbPathDef = pathDef;
    }


    public String getName() {
        return name;
    }

    public String getDbName() {
        return dbName;
    }

    public boolean isKeyGen() {
        return isKeyGen;
    }

    public long getDefaultValue() {
        return defaultValue;
    }

    public long getModulus() {
        return modulus;
    }

    public String getFullPattern() {
        return dbPathDef.getPatternPrefix() + dbPathDef.getBasePath() + getDbName();
    }

    public String getFullFormat() {
        return dbPathDef.getFormatPrefix() + dbPathDef.getBasePath() + getDbName();
    }

    @Override
    public String toString() {
        return "{" +
                "fullName:" + getName() + ",\n" +
                "dbName:" + getDbName() + ",\n" +
                "pattern:" + getFullPattern() + ",\n" +
                "format:" + getFullFormat() + ",\n" +
                "}";
    }
}
