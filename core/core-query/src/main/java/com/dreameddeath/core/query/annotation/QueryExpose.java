/*
 * 	Copyright Christophe Jeunesse
 *
 * 	Licensed under the Apache License, Version 2.0 (the "License");
 * 	you may not use this file except in compliance with the License.
 * 	You may obtain a copy of the License at
 *
 * 	http://www.apache.org/licenses/LICENSE-2.0
 *
 * 	Unless required by applicable law or agreed to in writing, software
 * 	distributed under the License is distributed on an "AS IS" BASIS,
 * 	WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * 	See the License for the specific language governing permissions and
 * 	limitations under the License.
 *
 */

package com.dreameddeath.core.query.annotation;

import com.dreameddeath.core.model.dto.annotation.processor.model.FieldGenMode;
import com.dreameddeath.core.model.dto.annotation.processor.model.SuperClassGenMode;
import com.dreameddeath.core.service.annotation.VersionStatus;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Created by Christophe Jeunesse on 27/12/2016.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface QueryExpose {
    String REST_EXPOSE_DTO_MODEL_TYPE = "query";
    FieldGenMode DEFAULT_OUTPUT_GEN_MODE = FieldGenMode.SIMPLE;
    SuperClassGenMode DEFAULT_SUPERCLASS_GEN_MODE = SuperClassGenMode.AUTO;

    String rootPath();
    String domain() default "";
    String name() default  "";
    String version() default "1.0.0";
    VersionStatus status() default VersionStatus.STABLE;
    FieldGenMode defaultOutputFieldMode() default FieldGenMode.SIMPLE;
    SuperClassGenMode superClassGenMode() default SuperClassGenMode.AUTO;
    boolean notDirecltyExposed() default false;
    boolean forceGenerateMode() default false;
    String jsonTypeId() default "";

    boolean isClassRootHierarchy() default false;
}
