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

package com.dreameddeath.core.elasticsearch.dao;

import com.dreameddeath.core.model.document.CouchbaseDocument;
import org.elasticsearch.search.SearchHit;

/**
 * Created by Christophe Jeunesse on 19/07/2015.
 */
public class ElasticSearchResultHit<T extends CouchbaseDocument> {
    private ElasticSearchDao<T> elasticSearchDao;
    private SearchHit hit;
    private volatile boolean mappingAttempted;
    private T obj;

    public ElasticSearchResultHit(ElasticSearchDao<T> dao, SearchHit hit) {
        elasticSearchDao = dao;
        this.hit = hit;
        mappingAttempted = false;
    }

    public float getScore() {
        return hit.score();
    }

    public String getKey() {
        return hit.getId();
    }

    public T get() {
        if (!mappingAttempted) {
            synchronized (this) {
                if (!mappingAttempted) {
                    if (hit.hasSource()) {
                        obj = elasticSearchDao.getTranscoder().decode(hit.source());
                    }
                    mappingAttempted = true;
                }
            }
        }
        return obj;
    }
}
