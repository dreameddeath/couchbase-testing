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
import com.couchbase.client.java.view.ViewResult;
import com.couchbase.client.java.view.ViewRow;
import com.dreameddeath.core.dao.model.view.IViewQuery;
import com.dreameddeath.core.dao.model.view.IViewQueryResult;
import com.dreameddeath.core.dao.model.view.IViewQueryRow;
import com.dreameddeath.core.model.document.CouchbaseDocument;

import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * Created by Christophe Jeunesse on 27/12/2014.
 */
public class ViewQueryResult<TKEY,TVALUE,TDOC extends CouchbaseDocument> implements IViewQueryResult<TKEY,TVALUE,TDOC> {
    IViewQuery<TKEY,TVALUE,TDOC> query;
    ViewResult result;

    public ViewQueryResult(IViewQuery<TKEY,TVALUE,TDOC> query,ViewResult result){
        this.query =query;
        this.result = result;
    }

    public static <TKEY,TVALUE,TDOC extends CouchbaseDocument> IViewQueryResult<TKEY,TVALUE,TDOC> from(IViewQuery<TKEY,TVALUE,TDOC> query,ViewResult result){
        return new ViewQueryResult<>(query,result);
    }

    protected Stream<IViewQueryRow<TKEY, TVALUE, TDOC>> buildStream(){
        Iterable<ViewRow> it = ()->result.rows();
        return StreamSupport.stream(it.spliterator(),false).map(vr -> query.getDao().map(vr));
    }

    @Override
    public List<IViewQueryRow<TKEY, TVALUE, TDOC>> getAllRows() {
        return buildStream().collect(Collectors.<IViewQueryRow<TKEY, TVALUE, TDOC>>toList());
    }

    @Override
    public Iterator<IViewQueryRow<TKEY, TVALUE, TDOC>> getRows() {
        return buildStream().iterator();
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
    public JsonObject getErrorInfo(){
        return result.error();
    }


    @Override public IViewQuery getQuery() {
        return query;
    }
    @Override public IViewQuery getQueryForNext(int nb) {
        return query.next(nb);
    }
}
