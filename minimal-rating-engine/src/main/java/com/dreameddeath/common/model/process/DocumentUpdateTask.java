package com.dreameddeath.common.model.process;

import com.dreameddeath.common.annotation.DocumentProperty;
import com.dreameddeath.common.model.document.CouchbaseDocument;
import com.dreameddeath.common.model.property.ImmutableProperty;
import com.dreameddeath.common.model.property.Property;

/**
 * Created by ceaj8230 on 21/05/2014.
 */
public abstract class DocumentUpdateTask<T extends CouchbaseDocument> extends AbstractTask{
    @DocumentProperty("docKey")
    private Property<String> _docKey=new ImmutableProperty<String>(DocumentUpdateTask.this);

    public String getDocId(){return _docKey.get(); }
    public DocumentUpdateTask<T> setDocId(String docKey){_docKey.set(docKey); return this;}
    public T getDocument(){
        return (T)this.getParentJob().getSession().get(_docKey.get());
    }


    @Override
    public final void cleanup(){
        getDocument().cleanupAttachedTaskRef(this);
        getDocument().save();
    }

    @Override
    public final void process(){
        CouchbaseDocumentAttachedTaskRef reference = getDocument().getAttachedTaskRef(this);
        if(reference==null){
            processDocument();
            CouchbaseDocumentAttachedTaskRef attachedTaskRef = new CouchbaseDocumentAttachedTaskRef();
            attachedTaskRef.setJobKey(getParentJob().getKey());
            attachedTaskRef.setTaskId(this.getUid());
            getDocument().addAttachedTaskRef(attachedTaskRef);
            getDocument().save();
        }
    }

    public abstract void processDocument();
}
