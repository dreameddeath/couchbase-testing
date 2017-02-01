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

package com.dreameddeath.core.dao.annotation;

import com.dreameddeath.core.dao.annotation.processor.IDaoGeneratorPlugin;
import com.dreameddeath.core.dao.annotation.processor.model.GlobalDef;

import javax.annotation.processing.Messager;
import javax.lang.model.element.Element;

/**
 * Created by CEAJ8230 on 28/01/2017.
 */
public class DaoGlobalGeneratorPluginTestImpl implements IDaoGeneratorPlugin {
    @Override
    public void manage(GlobalDef globalDef, Messager messager, Element element) {
        globalDef.getDaoDef().addPluginAnnotationStrings("@DaoGlobalGenPluginAnnot(\"a tested generator\")");
        globalDef.getDaoDef().addPluginImportName("com.dreameddeath.core.dao.annotation.DaoGlobalGenPluginAnnot");
    }
}
