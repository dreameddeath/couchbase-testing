package com.dreameddeath.core.process.document;

import com.dreameddeath.core.annotation.DocumentProperty;
import com.dreameddeath.core.exception.dao.DaoException;
import com.dreameddeath.core.exception.dao.ValidationException;
import com.dreameddeath.core.exception.process.TaskExecutionException;
import com.dreameddeath.core.exception.storage.StorageException;
import com.dreameddeath.core.model.document.CouchbaseDocument;
import com.dreameddeath.core.model.property.Property;
import com.dreameddeath.core.model.property.impl.ImmutableProperty;
import com.dreameddeath.core.process.common.AbstractTask;

/**
 * Created by Christophe Jeunesse on 21/05/2014.
 */
public abstract class DocumentCreateTask<T extends CouchbaseDocument> extends AbstractTask {
    @DocumentProperty("docKey")
    private Property<String> _docKey=new ImmutableProperty<String>(DocumentCreateTask.this);

    public String getDocKey(){return _docKey.get(); }
    public void setDocKey(String docKey){_docKey.set(docKey); }
    public T getDocument() throws DaoException,StorageException{
        return (T)this.getParentJob().getMeta().getSession().get(_docKey.get());
    }


    @Override
    public boolean process() throws TaskExecutionException{
        try {
            //Recovery mode
            if(getDocKey()!=null){
                if(getDocument()!=null){
                    return false;
                }
            }

            T doc = buildDocument();
            //Prebuild key
            setDocKey(doc.getMeta().getSession().buildKey(doc).getMeta().getKey());
            //Attach it to the document
            getParentJob().getMeta().getSession().save(getParentJob());
            //Save Document afterwards
            doc.getMeta().getSession().save(doc);
        }
        catch(ValidationException e){
            throw new TaskExecutionException(this,State.PROCESSED,"Validation error", e);
        }
        catch(DaoException e){
            throw new TaskExecutionException(this,State.PROCESSED,"Dao error", e);
        }
        catch(StorageException e){
            throw new TaskExecutionException(this,State.PROCESSED,"Dao error", e);
        }
        return false; //No need to save (retry allowed)
    }

    protected abstract T buildDocument() throws DaoException,StorageException;

}
