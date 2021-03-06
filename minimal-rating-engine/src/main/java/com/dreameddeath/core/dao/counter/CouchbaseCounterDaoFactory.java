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

package com.dreameddeath.core.dao.counter;

import com.dreameddeath.core.dao.exception.dao.DaoNotFoundException;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;

/**
 * Created by Christophe Jeunesse on 02/09/2014.
 */
public class CouchbaseCounterDaoFactory {
    private Map<Pattern,CouchbaseCounterDao> patternsMap
            = new ConcurrentHashMap<Pattern,CouchbaseCounterDao>();

    public void addDao(CouchbaseCounterDao dao){
        patternsMap.put(Pattern.compile("^"+dao.getKeyPattern()+"$"),dao);
    }

    public CouchbaseCounterDao getDaoForKey(String key) throws DaoNotFoundException {
        for(Pattern pattern:patternsMap.keySet()){
            if(pattern.matcher(key).matches()){
                return patternsMap.get(pattern);
            }
        }
        throw new DaoNotFoundException(key, DaoNotFoundException.Type.COUNTER);
    }

}
