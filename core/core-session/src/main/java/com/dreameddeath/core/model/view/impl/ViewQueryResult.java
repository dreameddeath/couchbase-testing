package com.dreameddeath.core.model.view.impl;

import com.couchbase.client.java.view.ViewResult;
import com.dreameddeath.core.model.document.CouchbaseDocument;
import com.dreameddeath.core.model.view.IViewQuery;
import com.dreameddeath.core.model.view.IViewQueryResult;
import com.dreameddeath.core.model.view.IViewQueryRow;

import java.util.List;

/**
 * Created by ceaj8230 on 19/12/2014.
 */
public class ViewQueryResult<TKEY,TVALUE,TDOC extends CouchbaseDocument> implements IViewQueryResult<TKEY,TVALUE,TDOC> {
    private IViewQuery _srcQuery;
    private ViewResult _viewRawResult;


    @Override
    public List<IViewQueryRow<TKEY,TVALUE,TDOC>> getRows() {
        return null;
    }

    @Override
    public IViewQuery<TKEY,TVALUE,TDOC> getQuery() {
        return null;
    }

    @Override
    public IViewQuery<TKEY,TVALUE,TDOC> getQueryForNext(int nb) {
        return null;
    }

    @Override
    public IViewQuery<TKEY,TVALUE,TDOC> getQueryForPrevious(int nb) {
        return null;
    }
}
