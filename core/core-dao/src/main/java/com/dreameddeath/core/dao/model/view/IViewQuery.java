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

package com.dreameddeath.core.dao.model.view;

import com.couchbase.client.java.view.ViewQuery;
import com.dreameddeath.core.dao.view.CouchbaseViewDao;
import com.dreameddeath.core.model.document.CouchbaseDocument;

import java.util.Collection;

/**
 * Created by Christophe Jeunesse on 19/12/2014.
 */
public interface IViewQuery<TKEY,TVALUE,TDOC extends CouchbaseDocument> {
    IViewQuery<TKEY,TVALUE,TDOC> withKey(TKEY key);
    IViewQuery<TKEY,TVALUE,TDOC> withKeys(Collection<TKEY> key);
    IViewQuery<TKEY,TVALUE,TDOC> withStartKey(TKEY key);
    IViewQuery<TKEY,TVALUE,TDOC> withEndKey(TKEY key, boolean isInclusive);
    IViewQuery<TKEY,TVALUE,TDOC> withDescending(boolean desc);
    IViewQuery<TKEY,TVALUE,TDOC> withOffset(int nb);
    IViewQuery<TKEY,TVALUE,TDOC> withLimit(int nb);
    IViewQuery<TKEY,TVALUE,TDOC> syncWithDoc();
    ViewQuery toCouchbaseQuery();
    CouchbaseViewDao<TKEY,TVALUE,TDOC> getDao();

    IViewQuery<TKEY,TVALUE,TDOC> next(int nb);
}
