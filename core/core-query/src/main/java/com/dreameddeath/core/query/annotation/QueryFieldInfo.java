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

package com.dreameddeath.core.query.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Created by Christophe Jeunesse on 29/12/2016.
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface QueryFieldInfo {
    /**
     * Allow the renaming of the field in interfaces
     * @return the external name of the field
     */
    String value() default "";
    /**
     * allow the unwrapping of the field (ignore the level)
     * @return the marker telling to unwrap or not
     */
    boolean unwrap() default false;
    /**
     * allow to field field
     * @return the marker telling to unwrap or not
     */
    boolean isPrivate() default false;


    /**
     * determine the way to export fields
     * @return the mode of field export/filter
     */
    QueryFieldFilteringMode mode() default QueryFieldFilteringMode.INHERIT;
}
