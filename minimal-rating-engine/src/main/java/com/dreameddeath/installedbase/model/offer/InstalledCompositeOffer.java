package com.dreameddeath.installedbase.model.offer;

import com.dreameddeath.core.annotation.DocumentProperty;
import com.dreameddeath.core.model.property.impl.ArrayListProperty;
import com.dreameddeath.core.model.property.ListProperty;

import java.util.Collection;
import java.util.List;

/**
 * Created by ceaj8230 on 31/08/2014.
 */
public class InstalledCompositeOffer extends InstalledOffer {
    /**
     *  children : List of children offers id
     */
    @DocumentProperty("children")
    private ListProperty<String> _children = new ArrayListProperty<String>(InstalledCompositeOffer.this);

    // Children Accessors
    public List<String> getChildren() { return _children.get(); }
    public void setChildren(Collection<String> vals) { _children.set(vals); }
    public boolean addChildren(String val){ return _children.add(val); }
    public boolean removeChildren(String val){ return _children.remove(val); }

}
