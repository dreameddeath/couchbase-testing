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

package com.dreameddeath.core.dao.utils;

import com.dreameddeath.compile.tools.annotation.processor.reflection.AbstractClassInfo;
import com.dreameddeath.compile.tools.annotation.processor.reflection.ClassInfo;
import com.dreameddeath.core.dao.annotation.DaoForClass;
import com.dreameddeath.core.model.util.CouchbaseDocumentReflection;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * Created by Christophe Jeunesse on 09/08/2015.
 */
public class DaoUtils {
    public static String ROOT_FILENAME_CLASS = "META-INF/dao/perClassRegistering/";

    public static String getTargetDaoRegisteringFilename(DaoForClass annot){
        AbstractClassInfo classInfo = AbstractClassInfo.getClassInfoFromAnnot(annot,DaoForClass::value);
        return ROOT_FILENAME_CLASS+classInfo.getFullName();
    }

    public static ClassInfo getDaoFromClass(CouchbaseDocumentReflection docReflexion){
        String filename = ROOT_FILENAME_CLASS+docReflexion.getClassInfo().getFullName();
        InputStream is =Thread.currentThread().getContextClassLoader().getResourceAsStream(filename);
        if(is==null){
            if(docReflexion.getSuperclassReflection()!=null){
                return getDaoFromClass(docReflexion.getSuperclassReflection());
            }
            else{
                return null;
            }
        }
        else {
            BufferedReader fileReader = new BufferedReader(new InputStreamReader(is));
            try {
                String className = fileReader.readLine();
                return (ClassInfo) AbstractClassInfo.getClassInfo(className);
            } catch (ClassNotFoundException | IOException e) {
                throw new RuntimeException("Cannot find/read file <" + filename + "> for clazz <" + docReflexion.getClassInfo().getCompiledFileName() + ">", e);
            }
        }
    }
}
