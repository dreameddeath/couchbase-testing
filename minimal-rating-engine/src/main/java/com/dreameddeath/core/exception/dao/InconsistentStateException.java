package com.dreameddeath.core.exception.dao;

import com.dreameddeath.core.model.document.BaseCouchbaseDocument;

/**
 * Created by CEAJ8230 on 21/09/2014.
 */
@SuppressWarnings("StringBufferReplaceableByString")
public class InconsistentStateException extends DaoException {
    private BaseCouchbaseDocument _doc;
    public InconsistentStateException(BaseCouchbaseDocument doc,Throwable e){
        super(e);
        _doc=doc;
    }
    public InconsistentStateException(BaseCouchbaseDocument doc,String message, Throwable e){
        super(message,e);
        _doc=doc;
    }
    public InconsistentStateException(BaseCouchbaseDocument doc,String message){
        super(message);
        _doc=doc;
    }

    public BaseCouchbaseDocument getDocument(){
        return _doc;
    }

    @Override
    public String getMessage(){
        return new StringBuilder(super.getMessage()).append("\n The document was <").append(_doc).toString();
    }
}
