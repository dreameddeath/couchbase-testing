package com.dreameddeath.installedbase.model.common;

import com.dreameddeath.core.annotation.DocumentProperty;
import com.dreameddeath.core.model.common.BaseCouchbaseDocumentElement;
import com.dreameddeath.core.model.property.impl.ArrayListProperty;
import com.dreameddeath.core.model.property.ListProperty;
import com.dreameddeath.core.model.property.Property;
import com.dreameddeath.core.model.property.impl.StandardProperty;
import org.joda.time.DateTime;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

/**
 * Created by ceaj8230 on 10/08/2014.
 */
public abstract class InstalledItem<T extends InstalledItemRevision> extends BaseCouchbaseDocumentElement {
    @DocumentProperty("id")
    private Property<String> _id = new StandardProperty<String>(InstalledItem.this, UUID.randomUUID().toString());
    @DocumentProperty("creationDate")
    private Property<DateTime> _creationDate = new StandardProperty<DateTime>(InstalledItem.this);
    @DocumentProperty("lastModificationDate")
    private Property<DateTime> _lastModificationDate = new StandardProperty<DateTime>(InstalledItem.this);
    /**
     *  status :
     */
    @DocumentProperty("status")
    private Property<InstalledStatus> _status = new StandardProperty<InstalledStatus>(InstalledItem.this);
    /**
     *  code : The code of the item
     */
    @DocumentProperty("code")
    private Property<String> _code = new StandardProperty<String>(InstalledItem.this);
    /**
     *  revisions : ItemRevisions
     */
    @DocumentProperty("revisions")
    private ListProperty<T> _revisions = new ArrayListProperty<T>(InstalledItem.this);

    // id accessors
    public String getId() { return _id.get(); }
    public void setId(String val) { _id.set(val); }

    // creationDate accessors
    public DateTime getCreationDate() { return _creationDate.get(); }
    public void setCreationDate(DateTime val) { _creationDate.set(val); }

    // lastModificationDate accessors
    public DateTime getLastModificationDate() { return _lastModificationDate.get(); }
    public void setLastModificationDate(DateTime val) { _lastModificationDate.set(val); }

    // status accessors
    public InstalledStatus getStatus() { return _status.get(); }
    public void setStatus(InstalledStatus val) { _status.set(val); }

    // code accessors
    public String getCode() { return _code.get(); }
    public void setCode(String val) { _code.set(val); }

    // Revisions Accessors
    public List<T> getRevisions() { return _revisions.get(); }
    public void setRevisions(Collection<T> vals) { _revisions.set(vals); }
    public boolean addRevisions(T val){ return _revisions.add(val); }
    public boolean removeRevisions(T val){ return _revisions.remove(val); }

}
