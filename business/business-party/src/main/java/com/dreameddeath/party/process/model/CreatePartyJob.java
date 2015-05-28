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

package com.dreameddeath.party.process.model;

import com.dreameddeath.core.annotation.DocumentDef;
import com.dreameddeath.core.process.model.AbstractJob;
import com.dreameddeath.core.process.model.DocumentCreateTask;
import com.dreameddeath.core.process.model.EmptyJobResult;
import com.dreameddeath.party.model.base.Party;

/**
 * Created by Christophe Jeunesse on 01/08/2014.
 */
@DocumentDef(domain = "party",version="1.0.0")
public class CreatePartyJob extends AbstractJob<CreatePartyRequest,EmptyJobResult> {
    @Override
    public CreatePartyRequest newRequest(){return new CreatePartyRequest();}
    @Override
    public EmptyJobResult newResult(){return new EmptyJobResult();}

    @DocumentDef(domain="party",version="1.0.0")
    public static class CreatePartyTask extends DocumentCreateTask<Party>{
    }
}
