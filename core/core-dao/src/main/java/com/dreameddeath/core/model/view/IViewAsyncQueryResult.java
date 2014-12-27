package com.dreameddeath.core.model.view;

import com.dreameddeath.core.model.document.CouchbaseDocument;
import rx.Observable;

/**
 * Created by CEAJ8230 on 27/12/2014.
 */
public interface IViewAsyncQueryResult<TKEY,TVALUE,TDOC extends CouchbaseDocument> {
    public Observable<IViewQueryRow<TKEY,TVALUE,TDOC>> getRows();

    public IViewQuery getQuery();
    public IViewQuery getQueryForNext(int nb);

}