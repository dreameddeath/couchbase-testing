package com.dreameddeath.installedbase.model;

import com.dreameddeath.core.annotation.DocumentProperty;
import com.dreameddeath.core.model.property.ArrayListProperty;
import com.dreameddeath.core.model.property.ListProperty;

import java.util.Collection;
import java.util.List;

/**
 * Created by ceaj8230 on 10/08/2014.
 */
public class InstalledProductService extends InstalledItem<InstalledProductServiceRevision> {
    /**
     *  functions : give the list of functions attached to the Product
     */
    @DocumentProperty("functions")
    private ListProperty<InstalledFunction> _functions = new ArrayListProperty<InstalledFunction>(InstalledProductService.this);

    // Functions Accessors
    public List<InstalledFunction> getFunctions() { return _functions.get(); }
    public void setFunctions(Collection<InstalledFunction> vals) { _functions.set(vals); }
    public boolean addFunctions(InstalledFunction val){ return _functions.add(val); }
    public boolean removeFunctions(InstalledFunction val){ return _functions.remove(val); }

}
