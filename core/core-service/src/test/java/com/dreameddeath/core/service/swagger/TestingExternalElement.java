package com.dreameddeath.core.service.swagger;

import com.dreameddeath.core.annotation.DocumentProperty;
import com.dreameddeath.core.model.document.CouchbaseDocumentElement;
import com.dreameddeath.core.model.property.ListProperty;
import com.dreameddeath.core.model.property.impl.ArrayListProperty;
import org.joda.time.DateTime;

import java.util.Collection;
import java.util.List;

/**
 * Created by CEAJ8230 on 03/03/2015.
 */
public class TestingExternalElement extends CouchbaseDocumentElement {
    /**
     *  date : DateTime test
     */
    @DocumentProperty("date")
    private ListProperty<DateTime> _date = new ArrayListProperty<DateTime>(TestingExternalElement.this);

    // Date Accessors
    public List<DateTime> getDate() { return _date.get(); }
    public void setDate(Collection<DateTime> vals) { _date.set(vals); }
    public boolean addDate(DateTime val){ return _date.add(val); }
    public boolean removeDate(DateTime val){ return _date.remove(val); }

}
