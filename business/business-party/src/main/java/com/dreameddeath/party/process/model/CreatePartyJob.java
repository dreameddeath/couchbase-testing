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

import com.dreameddeath.core.model.annotation.DocumentEntity;
import com.dreameddeath.core.model.annotation.DocumentProperty;
import com.dreameddeath.core.model.document.CouchbaseDocumentElement;
import com.dreameddeath.core.process.model.v1.base.AbstractJob;
import com.dreameddeath.core.process.model.v1.tasks.DocumentCreateTask;
import com.dreameddeath.party.model.base.Party;

/**
 * Created by Christophe Jeunesse on 01/08/2014.
 */
@DocumentEntity(domain = "party",version="1.0.0")
public class CreatePartyJob extends AbstractJob {
    @DocumentProperty("type")
    public Type type;
    @DocumentProperty("person")
    public Person person;
    @DocumentProperty("organization")
    public Organization organization;

    public static class Person extends CouchbaseDocumentElement {
        @DocumentProperty("firstName")
        public String firstName;
        @DocumentProperty("lastName")
        public String lastName;
    }

    public static class Organization extends CouchbaseDocumentElement {
        @DocumentProperty("brand")
        public String brand;
        @DocumentProperty("tradingName")
        public String tradingName;
    }

    public enum Type{
        person, organization
    }


    @DocumentEntity(domain="party",version="1.0.0")
    public static class CreatePartyTask extends DocumentCreateTask<Party> {
    }
}
