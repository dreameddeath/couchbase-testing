/*
 * Copyright Christophe Jeunesse
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package com.dreameddeath.core.model.view;

import com.couchbase.client.java.view.ViewQuery;
import com.dreameddeath.core.dao.view.CouchbaseViewDao;
import com.dreameddeath.core.model.document.CouchbaseDocument;

import java.util.Collection;

/**
 * Created by ceaj8230 on 19/12/2014.
 */
public interface IViewQuery<TKEY,TVALUE,TDOC extends CouchbaseDocument> {
    public IViewQuery<TKEY,TVALUE,TDOC> withKey(TKEY key);
    public IViewQuery<TKEY,TVALUE,TDOC> withKeys(Collection<TKEY> key);
    public IViewQuery<TKEY,TVALUE,TDOC> withStartKey(TKEY key);
    public IViewQuery<TKEY,TVALUE,TDOC> withEndKey(TKEY key,boolean isInclusive);
    public IViewQuery<TKEY,TVALUE,TDOC> withDescending(boolean desc);
    public IViewQuery<TKEY,TVALUE,TDOC> withOffset(int nb);
    public IViewQuery<TKEY,TVALUE,TDOC> withLimit(int nb);
    public IViewQuery<TKEY,TVALUE,TDOC> syncWithDoc();
    public ViewQuery toCouchbaseQuery();
    public CouchbaseViewDao<TKEY,TVALUE,TDOC> getDao();

    public IViewQuery<TKEY,TVALUE,TDOC> next(int nb);
}
