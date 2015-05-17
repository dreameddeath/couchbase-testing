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

package com.dreameddeath.core.dao.helper.annotation.processor.model;

import com.dreameddeath.core.dao.helper.annotation.Counter;
import com.dreameddeath.core.model.util.CouchbaseDocumentReflection;

/**
 * Created by CEAJ8230 on 10/04/2015.
 */
public class CounterDef {
    private String _name;
    private String _dbName;
    private String _pattern;
    private DbPathDef _dbPathDef;
    private boolean _isKeyGen;
    private int _defaultValue;
    private long _modulus;

    public CounterDef(CouchbaseDocumentReflection docReflection, Counter annot, DbPathDef pathDef) {
        _name = annot.name();
        _dbName = annot.name();
        _isKeyGen = annot.isKeyGen();
        _defaultValue = annot.defaultValue();
        _modulus = annot.modulus();
        _dbPathDef = pathDef;
    }


    public String getName() {
        return _name;
    }

    public String getDbName() {
        return _dbName;
    }

    public boolean isKeyGen() {
        return _isKeyGen;
    }

    public int getDefaultValue() {
        return _defaultValue;
    }

    public long getModulus() {
        return _modulus;
    }

    public String getFullPattern() {
        return _dbPathDef.getPatternPrefix() + _dbPathDef.getBasePath() + getDbName();
    }

    public String getFullFormat() {
        return _dbPathDef.getFormatPrefix() + _dbPathDef.getBasePath() + getDbName();
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
