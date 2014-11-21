package com.dreameddeath.installedbase.utils;

import com.dreameddeath.installedbase.model.common.InstalledItem;

/**
 * Created by ceaj8230 on 21/10/2014.
 */
public interface InstalledBaseVisitor {
    /**
     * @param item the item to visit
     * @return false to stop the processing immediately
     */
    public boolean visit(InstalledItem item);


}
