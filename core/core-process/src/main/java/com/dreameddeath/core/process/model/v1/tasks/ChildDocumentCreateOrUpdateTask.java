package com.dreameddeath.core.process.model.v1.tasks;

import com.dreameddeath.core.dao.session.ICouchbaseSession;
import com.dreameddeath.core.model.annotation.DocumentProperty;
import com.dreameddeath.core.model.document.CouchbaseDocument;
import com.dreameddeath.core.model.property.Property;
import com.dreameddeath.core.model.property.impl.ImmutableProperty;
import io.reactivex.Single;

public class ChildDocumentCreateOrUpdateTask<TCHILD extends CouchbaseDocument,TPARENT extends CouchbaseDocument> extends DocumentCreateOrUpdateTask<TCHILD>  {
    public ChildDocumentCreateOrUpdateTask(){

    }

    public ChildDocumentCreateOrUpdateTask(String parentKey){
        setParentDocKey(parentKey);
    }

    /**
     *  parentDocKey : Parent document key
     */
    @DocumentProperty("parent")
    private Property<String> parentDocKey = new ImmutableProperty<>(ChildDocumentCreateOrUpdateTask.this);

    /**
     * Getter of parentDocKey
     * @return the content
     */
    public String getParentDocKey() { return parentDocKey.get(); }
    /**
     * Setter of parentDocKey
     * @param parentKey the key of the parent
     */
    public void setParentDocKey(String parentKey) { parentDocKey.set(parentKey); }

    public Single<TPARENT> getParentDocument(ICouchbaseSession session){return session.asyncGet(getParentDocKey());}
}
