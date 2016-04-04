package com.dreameddeath.installedbase.service.utils;

import com.dreameddeath.installedbase.model.common.InstalledItem;
import com.dreameddeath.installedbase.model.common.InstalledItemRevision;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by Christophe Jeunesse on 02/04/2016.
 */
public class InstalledItemRevisionsToApply<TREV extends InstalledItemRevision,TITEM extends InstalledItem<TREV>> {
    private final TITEM parent;
    private final List<TREV> revisionsToApply;

    public InstalledItemRevisionsToApply(TITEM item){
        parent=item;
        revisionsToApply=new ArrayList<>(item.getRevisions().size());
    }

    public void addRevisionToApply(TREV revision){
        revisionsToApply.add(revision);
    }

    public List<TREV> getRevisionsToApply(){
        return Collections.unmodifiableList(revisionsToApply);
    }
}
