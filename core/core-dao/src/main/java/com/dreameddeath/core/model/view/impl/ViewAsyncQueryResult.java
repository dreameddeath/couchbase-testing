package com.dreameddeath.core.model.view.impl;

import com.couchbase.client.java.view.AsyncViewResult;
import com.dreameddeath.core.model.document.CouchbaseDocument;
import com.dreameddeath.core.model.view.IViewAsyncQueryResult;
import com.dreameddeath.core.model.view.IViewQuery;
import com.dreameddeath.core.model.view.IViewQueryRow;
import rx.Observable;

/**
 * Created by CEAJ8230 on 27/12/2014.
 */
public class ViewAsyncQueryResult<TKEY,TVALUE,TDOC extends CouchbaseDocument> implements IViewAsyncQueryResult<TKEY,TVALUE,TDOC> {
    IViewQuery<TKEY,TVALUE,TDOC> _query;
    AsyncViewResult _result;

    public ViewAsyncQueryResult(IViewQuery<TKEY,TVALUE,TDOC> query,AsyncViewResult result){
        _query =query;
        _result = result;
    }

    public static <TKEY,TVALUE,TDOC extends CouchbaseDocument> IViewAsyncQueryResult<TKEY,TVALUE,TDOC> from(IViewQuery<TKEY,TVALUE,TDOC> query,AsyncViewResult result){
        return new ViewAsyncQueryResult<>(query,result);
    }


    @Override public Observable<IViewQueryRow<TKEY, TVALUE, TDOC>> getRows() {return _result.rows().map(asyncViewRow-> _query.getDao().map(asyncViewRow));}
    @Override public IViewQuery getQuery() {
        return _query;
    }
    @Override public IViewQuery getQueryForNext(int nb) {
        return _query.next(nb);
    }

}
