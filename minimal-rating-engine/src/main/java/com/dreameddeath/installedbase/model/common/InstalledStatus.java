package com.dreameddeath.installedbase.model.common;

import com.dreameddeath.core.annotation.DocumentProperty;
import com.dreameddeath.core.model.common.BaseCouchbaseDocumentElement;
import com.dreameddeath.core.model.property.Property;
import com.dreameddeath.core.model.property.impl.StandardProperty;
import org.joda.time.DateTime;

/**
 * Created by ceaj8230 on 10/08/2014.
 */
public class InstalledStatus extends BaseCouchbaseDocumentElement {
    /**
     *  code : Status Code
     */
    @DocumentProperty("code")
    private Property<Code> _code = new StandardProperty<Code>(InstalledStatus.this);
    /**
     *  startDate : Start Validity date of the status
     */
    @DocumentProperty("startDate")
    private Property<DateTime> _startDate = new StandardProperty<DateTime>(InstalledStatus.this);
    /**
     *  endDate : End validity date of the status
     */
    @DocumentProperty("endDate")
    private Property<DateTime> _endDate = new StandardProperty<DateTime>(InstalledStatus.this);

    // code accessors
    public Code getCode() { return _code.get(); }
    public void setCode(Code val) { _code.set(val); }
    // startDate accessors
    public DateTime getStartDate() { return _startDate.get(); }
    public void setStartDate(DateTime val) { _startDate.set(val); }
    // endDate accessors
    public DateTime getEndDate() { return _endDate.get(); }
    public void setEndDate(DateTime val) { _endDate.set(val); }

    public enum Code{
        INITIALIZED,
        IN_ORDER,
        ACTIVE,
        SUSPENDED,
        REMOVED,
        CANCELLED,
        ABORTED
    }
}
