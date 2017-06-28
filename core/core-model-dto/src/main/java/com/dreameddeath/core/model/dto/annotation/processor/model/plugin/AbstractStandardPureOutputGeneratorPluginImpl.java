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

package com.dreameddeath.core.model.dto.annotation.processor.model.plugin;

import com.dreameddeath.core.model.dto.annotation.processor.model.FieldGenMode;

import java.lang.annotation.Annotation;

/**
 * Created by Christophe Jeunesse on 27/06/2017.
 */
public abstract class AbstractStandardPureOutputGeneratorPluginImpl<TROOT extends Annotation,TOUTFIELD extends Annotation> extends AbstractStandardGeneratorPlugin<TROOT, AbstractStandardPureOutputGeneratorPluginImpl.DummyInputAnnot, TOUTFIELD> {

    @Override
    protected FieldGenMode getDefaultInFieldGenMode() {
        throw new IllegalStateException("Should not occur for type "+getDtoModelType());
    }

    @Override
    protected Class<DummyInputAnnot> getFieldInputAnnot() {
        throw new IllegalStateException("Should not occur for type "+getDtoModelType());
    }

    @Override
    protected FieldGenMode getRootAnnotDefaultInputFieldMode(TROOT rootAnnotForKey) {
        throw new IllegalStateException("Should not occur for type "+getDtoModelType());
    }

    @Override
    protected String getInFieldAnnotVersion(DummyInputAnnot annot) {
        throw new IllegalStateException("Should not occur for type "+getDtoModelType());
    }

    @Override
    protected FieldGenMode getInFieldAnnotMode(DummyInputAnnot annot) {
        throw new IllegalStateException("Should not occur for type "+getDtoModelType());
    }

    @Override
    protected FieldGenMode getInFieldAnnotUnwrappedDefaultMode(DummyInputAnnot annot) {
        throw new IllegalStateException("Should not occur for type "+getDtoModelType());
    }

    @Override
    protected String getInFieldAnnotFieldName(DummyInputAnnot annot) {
        throw new IllegalStateException("Should not occur for type "+getDtoModelType());
    }
    
    public @interface DummyInputAnnot{
        
    }
}
