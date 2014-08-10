package com.dreameddeath.installedbase.model;

import com.dreameddeath.core.annotation.DocumentProperty;
import com.dreameddeath.core.model.document.CouchbaseDocumentElement;
import com.dreameddeath.core.model.property.Property;
import com.dreameddeath.core.model.property.StandardProperty;
import com.sun.org.apache.bcel.internal.classfile.Code;

/**
 * Created by ceaj8230 on 10/08/2014.
 */
public class InstalledStatus extends CouchbaseDocumentElement {
    /**
     *  code : Status Code
     */
    @DocumentProperty("code")
    private Property<Code> _code = new StandardProperty<Code>(InstalledStatus.this);

    // code accessors
    public Code getCode() { return _code.get(); }
    public void setCode(Code val) { _code.set(val); }

    public enum Code{
        INITIALIZED,
        IN_ORDER,
        SUSPENDED,
        REMOVED,
        CANCELLED,
        ABORTED
    }
}
