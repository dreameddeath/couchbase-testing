package com.dreameddeath.core.model.view;

import com.dreameddeath.core.model.document.CouchbaseDocument;

import java.util.List;

/**
 * Created by ceaj8230 on 19/12/2014.
 */
public interface IViewQueryResult<TKEY,TVALUE,TDOC extends CouchbaseDocument> {
    public List<IViewQueryRow<TKEY,TVALUE,TDOC>> getRows();

    public IViewQuery getQuery();
    public IViewQuery getQueryForNext(int nb);
    public IViewQuery getQueryForPrevious(int nb);

}
