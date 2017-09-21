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

package com.dreameddeath.core.model.dto.annotation.processor.model.unwrap;

import com.dreameddeath.core.model.annotation.DocumentEntity;
import com.dreameddeath.core.model.annotation.DocumentProperty;
import com.dreameddeath.core.model.dto.annotation.DtoInOutMode;
import com.dreameddeath.core.model.dto.annotation.processor.model.DtoGenerate;
import com.dreameddeath.core.model.dto.annotation.processor.model.DtoGenerateType;
import com.dreameddeath.core.model.dto.annotation.processor.model.SuperClassGenMode;

/**
 * Created by Christophe Jeunesse on 12/09/2017.
 */
@DocumentEntity(domain = "test",version = "1.0")
@DtoGenerate(mode = DtoInOutMode.BOTH,buildForTypes = {
        @DtoGenerateType(superClassGenMode = SuperClassGenMode.UNWRAP)
})
public class IntermediateUnwrapLevel1 extends RootClassStandard {
    @DocumentProperty
    public String unwrapLvl1;
}
