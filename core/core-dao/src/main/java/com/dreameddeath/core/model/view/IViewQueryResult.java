package com.dreameddeath.core.model.view;

import com.dreameddeath.core.model.document.CouchbaseDocument;

import java.util.Iterator;
import java.util.List;

/**
 * Created by ceaj8230 on 19/12/2014.
 */
public interface IViewQueryResult<TKEY,TVALUE,TDOC extends CouchbaseDocument> {
    public List<IViewQueryRow<TKEY,TVALUE,TDOC>> getAllRows();
    public Iterator<IViewQueryRow<TKEY,TVALUE,TDOC>> getRows();

    public IViewQuery getQuery();
    public IViewQuery getQueryForNext(int nb);
}
