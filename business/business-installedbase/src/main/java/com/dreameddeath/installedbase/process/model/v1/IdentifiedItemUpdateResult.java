package com.dreameddeath.installedbase.process.model.v1;

import com.dreameddeath.core.business.model.VersionedDocumentElement;
import com.dreameddeath.core.model.annotation.DocumentProperty;
import com.dreameddeath.core.model.property.ListProperty;
import com.dreameddeath.core.model.property.Property;
import com.dreameddeath.core.model.property.impl.ArrayListProperty;
import com.dreameddeath.core.model.property.impl.ImmutableProperty;

import java.util.Collection;
import java.util.List;

/**
 * Created by Christophe Jeunesse on 22/03/2016.
 */
public abstract class IdentifiedItemUpdateResult extends VersionedDocumentElement {
    /**
     *  tempId : TemporaryId of the element. Only for creation
     */
    @DocumentProperty("tempId")
    private Property<String> tempId = new ImmutableProperty<>(IdentifiedItemUpdateResult.this);
    /**
     *  id : id of the element
     */
    @DocumentProperty("id")
    private Property<String> id = new ImmutableProperty<>(IdentifiedItemUpdateResult.this);
    /**
     *  statuses : list the statuses updates done
     */
    @DocumentProperty("statuses")
    private ListProperty<StatusUpdateResult> statuses = new ArrayListProperty<>(IdentifiedItemUpdateResult.this);
    /**
     *  revisions : updates on the revisions of the items
     */
    @DocumentProperty("revisions")
    private ListProperty<RevisionUpdateResult> revisions = new ArrayListProperty<>(IdentifiedItemUpdateResult.this);

    /**
     * Getter of tempId
     * @return the content
     */
    public String getTempId() { return tempId.get(); }
    /**
     * Setter of tempId
     * @param val the new content
     */
    public void setTempId(String val) { tempId.set(val); }

    /**
     * Getter of id
     * @return the content
     */
    public String getId() { return id.get(); }
    /**
     * Setter of id
     * @param val the new content
     */
    public void setId(String val) { id.set(val); }

    /**
     * Getter of statuses
     * @return the content
     */
    public List<StatusUpdateResult> getStatuses() { return statuses.get(); }
    /**
     * Setter of statuses
     * @param vals the new collection of values
     */
    public void setStatuses(Collection<StatusUpdateResult> vals) { statuses.set(vals); }
    /**
     * Add a new entry to the property statuses
     * @param val the new entry to be added
     * @return true if the entry has been added
     */
    public boolean addStatus(StatusUpdateResult val){ return statuses.add(val); }
    /**
     * Add a new entry to the property statuses at the specified position
     * @param index the new entry to be added
     * @param val the new entry to be added
     * @return true if the entry has been added
     */
    public boolean addStatus(int index,StatusUpdateResult val){ return statuses.add(val); }
    /**
     * Remove an entry to the property statuses
     * @param val the entry to be remove
     * @return true if the entry has been removed
     */
    public boolean removeStatus(StatusUpdateResult val){ return statuses.remove(val); }
    /**
     * Remove an entry to the property statuses at the specified position
     * @param index the position of element to be removed
     * @return the entry removed if any
     */
    public StatusUpdateResult removeStatus(int index){ return statuses.remove(index); }



    /**
     * Getter of revisions
     * @return the content
     */
    public List<RevisionUpdateResult> getRevisions() { return revisions.get(); }
    /**
     * Setter of revisions
     * @param vals the new collection of values
     */
    public void setRevisions(Collection<RevisionUpdateResult> vals) { revisions.set(vals); }
    /**
     * Add a new entry to the property revisions
     * @param val the new entry to be added
     */
    public boolean addRevision(RevisionUpdateResult val){ return revisions.add(val); }
    /**
     * Remove an entry to the property revisions
     * @param val the entry to be remove
     * @return true if the entry has been removed
     */
    public boolean removeRevision(RevisionUpdateResult val){ return revisions.remove(val); }
}
