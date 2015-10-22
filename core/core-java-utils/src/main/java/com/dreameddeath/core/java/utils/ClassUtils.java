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

package com.dreameddeath.core.java.utils;

/**
 * Created by Christophe Jeunesse on 20/10/2015.
 */
public class ClassUtils {
    public static Class getCallerClass(int level) throws ClassNotFoundException {
        StackTraceElement[] stElements = Thread.currentThread().getStackTrace();
        String rawFQN = stElements[level+1].toString().split("\\(")[0];
        return Class.forName(rawFQN.substring(0, rawFQN.lastIndexOf('.')));
    }
}
