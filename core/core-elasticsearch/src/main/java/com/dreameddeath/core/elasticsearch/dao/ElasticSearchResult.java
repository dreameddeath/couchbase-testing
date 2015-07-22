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

package com.dreameddeath.core.elasticsearch.dao;

import com.dreameddeath.core.model.document.CouchbaseDocument;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.search.SearchHit;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Christophe Jeunesse on 19/07/2015.
 */
public class ElasticSearchResult<T extends CouchbaseDocument> {
    private final ElasticSearchDao<T> _elasticSearchDao;
    private final SearchResponse _esResult;
    private List<ElasticSearchResultHit<T>> _elasticSearchResultHitList = null;

    public ElasticSearchResult(ElasticSearchDao<T> dao, SearchResponse esResult) {
        _elasticSearchDao = dao;
        _esResult = esResult;
    }


    public long getTotalHitCount() {
        return _esResult.getHits().getTotalHits();
    }

    public List<ElasticSearchResultHit<T>> getList() {
        if (_elasticSearchResultHitList == null) {
            synchronized (this) {
                if (_elasticSearchResultHitList == null) {
                    _elasticSearchResultHitList = new ArrayList<>(_esResult.getHits().hits().length);
                    for (SearchHit hit : _esResult.getHits().hits()) {
                        _elasticSearchResultHitList.add(new ElasticSearchResultHit(_elasticSearchDao, hit));
                    }
                }
            }
        }
        return _elasticSearchResultHitList;
    }
}
