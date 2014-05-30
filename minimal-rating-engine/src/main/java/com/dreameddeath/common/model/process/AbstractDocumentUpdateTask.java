package com.dreameddeath.common.model.process;

import com.dreameddeath.common.annotation.DocumentProperty;
import com.dreameddeath.common.model.CouchbaseDocument;
import com.dreameddeath.common.model.property.ImmutableProperty;
import com.dreameddeath.common.model.property.Property;

/**
 * Created by ceaj8230 on 21/05/2014.
 */
public abstract class AbstractDocumentUpdateTask<T extends CouchbaseDocument> extends AbstractTask{
    @DocumentProperty("docId")
    private Property<String> _docId=new ImmutableProperty<String>(AbstractDocumentUpdateTask.this);

    public String getDocId(){return _docId.get(); }
    public void setDocId(String docId){_docId.set(docId); }
    public T getDocument(){
        return (T)this.getParentJob().getSession().get(_docId.get());
    }


}
