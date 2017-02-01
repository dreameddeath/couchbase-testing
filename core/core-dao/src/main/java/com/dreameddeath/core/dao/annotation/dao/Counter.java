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

package com.dreameddeath.core.dao.annotation.dao;

import java.lang.annotation.*;

/**
 * Created by Christophe Jeunesse on 08/01/2015.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Repeatable(Counters.class)
public @interface Counter {
    String name();
    String dbName();
    boolean isKeyGen() default false;
    long defaultValue() default 0L;
    long modulus() default 0;
    boolean rest() default false;//by default counters aren't available in rest
}
