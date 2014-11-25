package com.dreameddeath.core.process.business.model;

import com.dreameddeath.core.annotation.DocumentProperty;
import com.dreameddeath.core.annotation.NotNull;
import com.dreameddeath.core.exception.dao.DaoException;
import com.dreameddeath.core.exception.storage.StorageException;
import com.dreameddeath.core.model.business.BusinessCouchbaseDocument;
import com.dreameddeath.core.model.property.Property;
import com.dreameddeath.core.model.property.impl.ImmutableProperty;
import com.dreameddeath.core.model.process.AbstractTask;
import com.dreameddeath.core.session.ICouchbaseSession;

/**
 * Created by Christophe Jeunesse on 21/05/2014.
 */
public abstract class DocumentUpdateTask<T extends BusinessCouchbaseDocument> extends AbstractTask {
    @DocumentProperty("docKey") @NotNull
    private Property<String> _docKey=new ImmutableProperty<String>(DocumentUpdateTask.this);

    public String getDocKey(){return _docKey.get(); }
    public DocumentUpdateTask<T> setDocKey(String docKey){_docKey.set(docKey); return this;}

    public T getDocument(ICouchbaseSession session)throws DaoException, StorageException{return (T)session.get(getDocKey());}
}
