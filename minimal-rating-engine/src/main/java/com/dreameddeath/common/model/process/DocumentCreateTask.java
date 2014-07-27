package com.dreameddeath.common.model.process;

import com.dreameddeath.common.annotation.DocumentProperty;
import com.dreameddeath.common.model.document.CouchbaseDocument;
import com.dreameddeath.common.model.property.ImmutableProperty;
import com.dreameddeath.common.model.property.Property;

/**
 * Created by ceaj8230 on 21/05/2014.
 */
public abstract class DocumentCreateTask<T extends CouchbaseDocument> extends AbstractTask{
    @DocumentProperty("docKey")
    private Property<String> _docKey=new ImmutableProperty<String>(DocumentCreateTask.this);

    public String getDocId(){return _docKey.get(); }
    public void setDocId(String docKey){_docKey.set(docKey); }
    public T getDocument(){
        return (T)this.getParentJob().getSession().get(_docKey.get());
    }


    @Override
    public void process(){
        T doc = buildDocument();
        doc.save();
        setDocId(doc.getKey());
    }

    public abstract T buildDocument();

}
