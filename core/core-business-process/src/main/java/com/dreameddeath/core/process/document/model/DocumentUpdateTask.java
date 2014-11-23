package com.dreameddeath.core.process.document.model;

import com.dreameddeath.core.annotation.DocumentProperty;
import com.dreameddeath.core.annotation.NotNull;
import com.dreameddeath.core.model.document.CouchbaseDocument;
import com.dreameddeath.core.model.property.Property;
import com.dreameddeath.core.model.property.impl.ImmutableProperty;
import com.dreameddeath.core.model.process.AbstractTask;

/**
 * Created by Christophe Jeunesse on 21/05/2014.
 */
public abstract class DocumentUpdateTask<T extends CouchbaseDocument> extends AbstractTask {
    @DocumentProperty("docKey") @NotNull
    private Property<String> _docKey=new ImmutableProperty<String>(DocumentUpdateTask.this);

    public String getDocKey(){return _docKey.get(); }
    public DocumentUpdateTask<T> setDocKey(String docKey){_docKey.set(docKey); return this;}
}
