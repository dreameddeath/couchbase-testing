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

package com.dreameddeath.compile.tools.annotation.processor.reflection;

import javax.lang.model.element.TypeElement;

/**
 * Created by Christophe Jeunesse on 24/04/2015.
 */
public class AnnotationInfo extends InterfaceInfo {

    public AnnotationInfo(Class elt) {
        super(elt);
    }

    public AnnotationInfo(TypeElement elt) {
        super(elt);
    }

}
