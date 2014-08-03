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

    public String getDocKey(){return _docKey.get(); }
    public void setDocKey(String docKey){_docKey.set(docKey); }
    public T getDocument(){
        return (T)this.getParentJob().getSession().get(_docKey.get());
    }


    @Override
    public boolean process(){
        //Recovery mode
        if(getDocKey()!=null){
            if(getDocument()!=null){
                return false;
            }
        }
        T doc = buildDocument();
        //Prebuild key
        setDocKey(doc.getSession().buildKey(doc).getKey());
        //Attach it to the document
        this.getParentJob().save();
        //Save Document afterwards
        doc.save();
        return false; //No need to save (retry allowed)
    }

    protected abstract T buildDocument();

}
