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

import com.dreameddeath.core.dao.annotation.dao.DaoEntity;
import com.dreameddeath.core.dao.annotation.dao.ParentEntity;
import com.dreameddeath.core.dao.utils.KeyPattern;
import com.dreameddeath.core.model.util.CouchbaseDocumentReflection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Christophe Jeunesse on 10/04/2015.
 */
public class DbPathDef {
    private static final Logger LOG = LoggerFactory.getLogger(DbPathDef.class);
    private final static String FORMAT_PATTERN_STR= "%(\\d+\\$)?([-#+ 0,(\\<]*)?(\\d+)?(\\.\\d+)?([tT])?([a-zA-Z%])";
    private final static Pattern FORMAT_PATTERN = Pattern.compile(FORMAT_PATTERN_STR);

    private String basePath;
    private String idFormat;
    private String idPattern;
    private String dbName;
    private String formatPrefix = "";
    private String patternPrefix = "";
    private KeyPattern keyPattern;
    private List<FormatType> formatTypes = new ArrayList<>();

    public DbPathDef(CouchbaseDocumentReflection docReflection) {
        EntityDef entityDef =new EntityDef(docReflection);
        dbName = entityDef.getDbName();
        DaoEntity annot = docReflection.getClassInfo().getAnnotation(DaoEntity.class);
        basePath = annot.dbPath();

        idFormat = annot.idFormat();
        idPattern = annot.idPattern();

        DbPathDef parentDbPath = null;
        ParentEntity parentAnnot = docReflection.getClassInfo().getAnnotation(ParentEntity.class);
        if(parentAnnot!=null) {
            CouchbaseDocumentReflection classInfo = CouchbaseDocumentReflection.getClassInfoFromAnnot(parentAnnot, ParentEntity::c);
            parentDbPath = new DbPathDef(classInfo);
        }

        if (parentDbPath != null) {
            formatPrefix = "%s" + parentAnnot.separator();
            patternPrefix = parentDbPath.getFullPattern() + parentAnnot.separator();
        }
        keyPattern = new KeyPattern(patternPrefix);
        Matcher matcher = FORMAT_PATTERN.matcher(getFullFormat());
        while(matcher.find()){
            String result = matcher.group();
            char lastChar = result.charAt(result.length()-1);
            if("sS".indexOf(lastChar)>=0){
                formatTypes.add(FormatType.STRING);
            }
            else if("doXx".indexOf(lastChar)>=0){
                formatTypes.add(FormatType.LONG);
            }
            else if("eEfgGaA".indexOf(lastChar)>=0){
                formatTypes.add(FormatType.FLOAT);
            }
            else if("tT".indexOf(lastChar)>=0){
                formatTypes.add(FormatType.DATETIME);
            }
            else if("bBhH".indexOf(lastChar)>=0){
                formatTypes.add(FormatType.RAW);
            }
        }
    }


    public String getFullPattern() {
        return patternPrefix + basePath + "{"+dbName+"Uid :"+ idPattern+"}";
    }

    public String getFullFormat() {
        return formatPrefix + basePath + idFormat;
    }

    public String getFormatPrefix() {
        return formatPrefix;
    }

    public String getPatternPrefix() {
        return patternPrefix;
    }

    public String getIdFormat() {
        return idFormat;
    }

    public String getIdPattern() {
        return idPattern;
    }

    public String getBasePath() {
        return basePath;
    }

    public KeyPattern getKeyPattern() {
        return keyPattern;
    }

    public List<FormatType> getFormatTypes() {
        return formatTypes;
    }

    public enum FormatType{
        LONG(true,"NumberUtils.asLong"),
        FLOAT(true,"NumberUtils.asFloat"),
        STRING(false,null),
        DATETIME(true,"DateUtils.asDate"),
        RAW(false,null);

        private final String formatter;
        private final boolean needFormat;

        FormatType(boolean needFormat,String formatter){
            this.needFormat=needFormat;
            this.formatter = formatter;
        }

        public String getFormatter() {
            return formatter;
        }

        public boolean needFormat() {
            return needFormat;
        }
    }
}
