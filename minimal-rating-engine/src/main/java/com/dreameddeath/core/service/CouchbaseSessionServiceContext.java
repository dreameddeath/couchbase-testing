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

package com.dreameddeath.core.service;

import com.dreameddeath.core.CouchbaseSession;
import com.dreameddeath.core.model.document.BaseCouchbaseDocument;
import org.joda.time.DateTime;

/**
 * Created by Christophe Jeunesse on 03/11/2014.
 */
public class CouchbaseSessionServiceContext {
    final private CouchbaseSession _session;

    public CouchbaseSessionServiceContext(CouchbaseSession session){
        _session = session;
    }

    public CouchbaseSession getSession(){return _session;}

    public <T extends BaseCouchbaseDocument> T newEntity(Class<T> clazz){
        return _session.newEntity(clazz);
    }

    public DateTime getCurrentDate(){
        return _session.getDateTimeService().getCurrentDate();
    }
}
