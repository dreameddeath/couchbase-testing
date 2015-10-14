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

package com.dreameddeath.core.dao.model.view.impl;

import com.couchbase.client.java.document.json.JsonObject;
import com.couchbase.client.java.view.AsyncViewResult;
import com.dreameddeath.core.dao.model.view.IViewAsyncQueryResult;
import com.dreameddeath.core.dao.model.view.IViewQuery;
import com.dreameddeath.core.dao.model.view.IViewQueryRow;
import com.dreameddeath.core.model.document.CouchbaseDocument;
import rx.Observable;

/**
 * Created by Christophe Jeunesse on 27/12/2014.
 */
public class ViewAsyncQueryResult<TKEY,TVALUE,TDOC extends CouchbaseDocument> implements IViewAsyncQueryResult<TKEY,TVALUE,TDOC> {
    IViewQuery<TKEY,TVALUE,TDOC> query;
    AsyncViewResult result;

    public ViewAsyncQueryResult(IViewQuery<TKEY,TVALUE,TDOC> query,AsyncViewResult result){
        this.query =query;
        this.result = result;
    }

    public static <TKEY,TVALUE,TDOC extends CouchbaseDocument> IViewAsyncQueryResult<TKEY,TVALUE,TDOC> from(IViewQuery<TKEY,TVALUE,TDOC> query,AsyncViewResult result){
        return new ViewAsyncQueryResult<>(query,result);
    }

    @Override
    public int getTotalRows(){
        return result.totalRows();
    }

    @Override
    public boolean getSuccess(){
        return result.success();
    }

    @Override
    public Observable<JsonObject> getErrorInfo(){
        return result.error();
    }

    @Override public Observable<IViewQueryRow<TKEY, TVALUE, TDOC>> getRows() {return result.rows().map(asyncViewRow-> query.getDao().map(asyncViewRow));}
    @Override public IViewQuery getQuery() {
        return query;
    }
    @Override public IViewQuery getQueryForNext(int nb) {
        return query.next(nb);
    }

}
