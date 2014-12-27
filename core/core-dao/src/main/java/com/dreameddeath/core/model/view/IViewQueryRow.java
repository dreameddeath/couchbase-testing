package com.dreameddeath.core.model.view;

import com.dreameddeath.core.exception.dao.DaoException;
import com.dreameddeath.core.exception.storage.StorageException;
import com.dreameddeath.core.model.document.CouchbaseDocument;
import com.dreameddeath.core.session.ICouchbaseSession;

/**
 * Created by ceaj8230 on 19/12/2014.
 */
public interface IViewQueryRow<TKEY,TVALUE,TDOC extends CouchbaseDocument> {
    public TKEY getKey();
    public TVALUE getValue();
    public String getDocKey();
    public TDOC getDoc(ICouchbaseSession session) throws DaoException,StorageException;
}
