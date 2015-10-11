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

package com.dreameddeath.testing.couchbase;

import com.dreameddeath.core.config.exception.ConfigPropertyValueNotFoundException;
import com.dreameddeath.core.couchbase.ICouchbaseBucket;
import com.dreameddeath.core.couchbase.ICouchbaseBucketFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Christophe Jeunesse on 11/10/2015.
 */
public class CouchbaseBucketFactorySimulator implements ICouchbaseBucketFactory {
    private Map<String,ICouchbaseBucket> _couchbaseBucketMap = new HashMap<>();
    @Override
    synchronized  public ICouchbaseBucket getBucket(String name) throws ConfigPropertyValueNotFoundException {
        return getBucket(name,null);
    }

    @Override
    synchronized  public ICouchbaseBucket getBucket(String name, String prefix) throws ConfigPropertyValueNotFoundException {
        return _couchbaseBucketMap.computeIfAbsent(name+((prefix==null)?"":("#"+prefix)),name1->
                new CouchbaseBucketSimulator(name,prefix));

    }
}
