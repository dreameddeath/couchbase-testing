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

package com.dreameddeath.infrastructure.daemon.servlet;

/**
 * Created by Christophe Jeunesse on 28/08/2015.
 */
public abstract class ServletUtils {
    public static final String LOCAL_WEBAPP_SRC="src/main/resources";


    public static String normalizePath(String path, boolean withEndingSlash){
        path = path.replaceAll("^/+","");
        path = path.replaceAll("/+$","");
        path = "/"+path+(withEndingSlash?"/":"");
        path = path.replaceAll("/{2,}","/");
        return path;
    }

    public static String normalizePath(String[] paths, boolean withEndingSlash){
        String fullPath = "/"+String.join("/",paths);
        return normalizePath(fullPath,withEndingSlash);
    }
}
