package com.dreameddeath.catalog.model;

import com.dreameddeath.core.annotation.DocumentProperty;
import com.dreameddeath.core.model.common.BaseCouchbaseDocumentElement;
import com.dreameddeath.core.model.property.Property;
import com.dreameddeath.core.model.property.impl.StandardProperty;
import com.sun.istack.internal.NotNull;

/**
 * Created by ceaj8230 on 05/09/2014.
 */
public class CatalogItemVersion extends BaseCouchbaseDocumentElement implements Comparable<CatalogItemVersion> {
    /**
     *  major : Major Version
     */
    @DocumentProperty("major")
    private Property<Integer> _major = new StandardProperty<Integer>(CatalogItemVersion.this,1);
    /**
     *  minor : Minor Version
     */
    @DocumentProperty("minor")
    private Property<Integer> _minor = new StandardProperty<Integer>(CatalogItemVersion.this,0);
    /**
     *  patch : Patch version
     */
    @DocumentProperty("patch")
    private Property<Integer> _patch = new StandardProperty<Integer>(CatalogItemVersion.this,0);

    // major accessors
    public Integer getMajor() { return _major.get(); }
    public void setMajor(Integer val) { _major.set(val); }
    // minor accessors
    public Integer getMinor() { return _minor.get(); }
    public void setMinor(Integer val) { _minor.set(val); }
    // patch accessors
    public Integer getPatch() { return _patch.get(); }
    public void setPatch(Integer val) { _patch.set(val); }

    @Override
    public int compareTo( @NotNull CatalogItemVersion target) {
        int result = getMajor().compareTo(target.getMajor());
        if(result==0) result = getMinor().compareTo(target.getMinor());
        if(result==0) result = getPatch().compareTo(target.getPatch());

        return result;
    }

}
