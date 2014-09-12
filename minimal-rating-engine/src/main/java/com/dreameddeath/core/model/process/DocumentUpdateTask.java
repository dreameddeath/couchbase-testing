package com.dreameddeath.core.model.process;

import com.dreameddeath.core.annotation.DocumentProperty;
import com.dreameddeath.core.annotation.NotNull;
import com.dreameddeath.core.exception.dao.DaoException;
import com.dreameddeath.core.exception.dao.ValidationException;
import com.dreameddeath.core.exception.process.TaskExecutionException;
import com.dreameddeath.core.exception.storage.StorageException;
import com.dreameddeath.core.model.document.CouchbaseDocument;
import com.dreameddeath.core.model.property.impl.ImmutableProperty;
import com.dreameddeath.core.model.property.Property;

/**
 * Created by Christophe Jeunesse on 21/05/2014.
 */
public abstract class DocumentUpdateTask<T extends CouchbaseDocument> extends AbstractTask{
    @DocumentProperty("docKey") @NotNull
    private Property<String> _docKey=new ImmutableProperty<String>(DocumentUpdateTask.this);

    public String getDocKey(){return _docKey.get(); }
    public DocumentUpdateTask<T> setDocKey(String docKey){_docKey.set(docKey); return this;}
    public T getDocument() throws DaoException,StorageException{
        return (T)this.getParentJob().getSession().get(_docKey.get());
    }

    @Override
    public final boolean process() throws TaskExecutionException{
        try {
            CouchbaseDocumentAttachedTaskRef reference = getDocument().getAttachedTaskRef(this);
            if (reference == null) {
                processDocument();
                CouchbaseDocumentAttachedTaskRef attachedTaskRef = new CouchbaseDocumentAttachedTaskRef();
                attachedTaskRef.setJobKey(getParentJob().getDocumentKey());
                attachedTaskRef.setJobClass(getParentJob().getClass().getName());
                attachedTaskRef.setTaskId(this.getUid());
                attachedTaskRef.setTaskClass(this.getClass().getName());
                getDocument().addAttachedTaskRef(attachedTaskRef);
                try {
                    getDocument().save();
                } catch (ValidationException e) {
                    throw new TaskExecutionException(this, this.getState(), "Updated Document Validation exception", e);
                }
                return true;
            }
        }
        catch(DaoException e){
            throw new TaskExecutionException(this, this.getState(), "Dao exception", e);
        }
        catch(StorageException e){
            throw new TaskExecutionException(this, this.getState(), "Storage exception", e);
        }
        return false;
    }

    @Override
    public final boolean cleanup() throws TaskExecutionException{
        try {
            getDocument().cleanupAttachedTaskRef(this);
            getDocument().save();
        }
        catch(ValidationException e){
            throw new TaskExecutionException(this,this.getState(),"Cleaned updated document Validation exception",e);
        }
        catch(DaoException e){
            throw new TaskExecutionException(this,this.getState(),"Error in dao",e);
        }
        catch(StorageException e){
            throw new TaskExecutionException(this,this.getState(),"Error in storage",e);
        }

        return false;
    }

    protected abstract void processDocument() throws DaoException,StorageException;
}
