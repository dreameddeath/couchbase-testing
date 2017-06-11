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

import com.dreameddeath.core.model.dto.annotation.DtoInOutMode;

import java.lang.annotation.*;

/**
 * Created by ceaj8230 on 02/02/2017.
 */
@Target({ElementType.TYPE,ElementType.ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Repeatable(DtoGenerates.class)
public @interface DtoGenerate {
    DtoInOutMode DEFAULT_MODE = DtoInOutMode.NONE;

    String targetModelPackageName() default "";
    String targetModelClassName() default "";
    DtoInOutMode mode() default DtoInOutMode.OUT;
    DtoGenerateType[] buildForTypes() default {};
}
