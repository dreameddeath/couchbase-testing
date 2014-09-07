package com.dreameddeath.installedbase.model.common;

import com.dreameddeath.core.annotation.DocumentProperty;
import com.dreameddeath.core.model.document.CouchbaseDocumentElement;
import com.dreameddeath.core.model.property.Property;
import com.dreameddeath.core.model.property.impl.StandardProperty;
import org.joda.time.DateTime;

/**
 * Created by ceaj8230 on 10/08/2014.
 */
public class InstalledValue extends CouchbaseDocumentElement {
    /**
     *  value : the value itself
     */
    @DocumentProperty("value")
    private Property<String> _value = new StandardProperty<String>(InstalledValue.this);
    /**
     *  startDate : The value startDate
     */
    @DocumentProperty("startDate")
    private Property<DateTime> _startDate = new StandardProperty<DateTime>(InstalledValue.this);
    /**
     *  endDate : The value endDate
     */
    @DocumentProperty("endDate")
    private Property<DateTime> _endDate = new StandardProperty<DateTime>(InstalledValue.this);
    /**
     *  keyType : Type of public key if applicable
     */
    @DocumentProperty("keyType")
    private Property<String> _keyType = new StandardProperty<String>(InstalledValue.this);

    // value accessors
    public String getValue() { return _value.get(); }
    public void setValue(String val) { _value.set(val); }

    // startDate accessors
    public DateTime getStartDate() { return _startDate.get(); }
    public void setStartDate(DateTime val) { _startDate.set(val); }

    // endDate accessors
    public DateTime getEndDate() { return _endDate.get(); }
    public void setEndDate(DateTime val) { _endDate.set(val); }

    // keyType accessors
    public String getKeyType() { return _keyType.get(); }
    public void setKeyType(String val) { _keyType.set(val); }

}
