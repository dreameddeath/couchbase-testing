package com.dreameddeath.installedbase.model.common;

import com.dreameddeath.core.annotation.DocumentProperty;
import com.dreameddeath.core.model.document.BaseCouchbaseDocumentElement;
import com.dreameddeath.core.model.property.ListProperty;
import com.dreameddeath.core.model.property.Property;
import com.dreameddeath.core.model.property.impl.ArrayListProperty;
import com.dreameddeath.core.model.property.impl.StandardProperty;

import java.util.Collection;
import java.util.List;

/**
 * Created by ceaj8230 on 10/08/2014.
 */
public abstract class InstalledAttribute extends BaseCouchbaseDocumentElement {
    /**
     *  code : The catalogue code of the attribute
     */
    @DocumentProperty("code")
    private Property<String> _code = new StandardProperty<String>(InstalledAttribute.this);
    /**
     *  values : List of values (and their history)
     */
    @DocumentProperty("values")
    private ListProperty<InstalledValue> _values = new ArrayListProperty<InstalledValue>(InstalledAttribute.this);

    // code accessors
    public String getCode() { return _code.get(); }
    public void setCode(String val) { _code.set(val); }

    // Values Accessors
    public List<InstalledValue> getValues() { return _values.get(); }
    public void setValues(Collection<InstalledValue> vals) { _values.set(vals); }
    public boolean addValues(InstalledValue val){ return _values.add(val); }
    public boolean removeValues(InstalledValue val){ return _values.remove(val); }

}
