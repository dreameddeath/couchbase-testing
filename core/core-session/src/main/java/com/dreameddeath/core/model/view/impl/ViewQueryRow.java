package com.dreameddeath.core.model.view.impl;

import com.dreameddeath.core.exception.dao.DaoException;
import com.dreameddeath.core.exception.storage.StorageException;
import com.dreameddeath.core.model.document.CouchbaseDocument;
import com.dreameddeath.core.model.view.IViewQueryRow;
import com.dreameddeath.core.session.ICouchbaseSession;

/**
 * Created by ceaj8230 on 19/12/2014.
 */
public class ViewQueryRow<TKEY,TVALUE,TDOC extends CouchbaseDocument> implements IViewQueryRow<TKEY,TVALUE,TDOC> {
    private TKEY _key;
    private TVALUE _value;
    private String _docKey;


    @Override
    public TKEY getKey() {
        return _key;
    }

    @Override
    public void setKey(TKEY key) {
        _key = key;
    }

    @Override
    public TVALUE getValue() {
        return _value;
    }

    @Override
    public void setValue(TVALUE value) {
        _value = value;
    }

    @Override
    public String getDocKey() {
        return _docKey;
    }

    @Override
    public void setDocKey(String docKey) {
        _docKey = docKey;
    }

    @Override
    public TDOC getDoc(ICouchbaseSession session) throws DaoException,StorageException {
        return (TDOC) session.get(getDocKey());
    }
}
