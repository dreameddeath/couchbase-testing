package com.dreameddeath.core.model.view.impl;

import com.couchbase.client.java.view.ViewResult;
import com.couchbase.client.java.view.ViewRow;
import com.dreameddeath.core.model.document.CouchbaseDocument;
import com.dreameddeath.core.model.view.IViewQuery;
import com.dreameddeath.core.model.view.IViewQueryResult;
import com.dreameddeath.core.model.view.IViewQueryRow;

import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * Created by CEAJ8230 on 27/12/2014.
 */
public class ViewQueryResult<TKEY,TVALUE,TDOC extends CouchbaseDocument> implements IViewQueryResult<TKEY,TVALUE,TDOC> {
    IViewQuery<TKEY,TVALUE,TDOC> _query;
    ViewResult _result;

    public ViewQueryResult(IViewQuery<TKEY,TVALUE,TDOC> query,ViewResult result){
        _query =query;
        _result = result;
    }

    public static <TKEY,TVALUE,TDOC extends CouchbaseDocument> IViewQueryResult<TKEY,TVALUE,TDOC> from(IViewQuery<TKEY,TVALUE,TDOC> query,ViewResult result){
        return new ViewQueryResult<>(query,result);
    }

    protected Stream<IViewQueryRow<TKEY, TVALUE, TDOC>> buildStream(){
        Iterable<ViewRow> it = ()->_result.rows();
        return StreamSupport.stream(it.spliterator(),false).map(vr -> _query.getDao().map(vr));
    }

    @Override
    public List<IViewQueryRow<TKEY, TVALUE, TDOC>> getAllRows() {
        return buildStream().collect(Collectors.<IViewQueryRow<TKEY, TVALUE, TDOC>>toList());
    }

    @Override
    public Iterator<IViewQueryRow<TKEY, TVALUE, TDOC>> getRows() {
        return buildStream().iterator();
    }


    @Override public IViewQuery getQuery() {
        return _query;
    }
    @Override public IViewQuery getQueryForNext(int nb) {
        return _query.next(nb);
    }
}
