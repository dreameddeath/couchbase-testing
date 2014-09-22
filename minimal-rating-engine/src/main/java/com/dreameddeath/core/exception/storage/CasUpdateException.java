package com.dreameddeath.core.exception.storage;

import com.dreameddeath.core.model.common.BaseCouchbaseDocument;
import net.spy.memcached.CASResponse;

/**
 * Created by Christophe Jeunesse on 05/08/2014.
 */
public class CasUpdateException extends DocumentStorageException {
    private CASResponse _casResponse;

    public CasUpdateException(BaseCouchbaseDocument doc,CASResponse response){ super(doc); _casResponse = response;}
    public CasUpdateException(BaseCouchbaseDocument doc,CASResponse response,String message){ super(doc,message);_casResponse = response;}
    public CasUpdateException(BaseCouchbaseDocument doc,CASResponse response,String message,Throwable e){ super(doc,message,e);_casResponse = response;}
    public CasUpdateException(BaseCouchbaseDocument doc,CASResponse response,Throwable e){ super(doc,e);_casResponse = response;}

    public CASResponse getCasResponse(){  return _casResponse;}
    @Override
    public String getMessage(){
        StringBuilder builder =new StringBuilder(super.getMessage());
        builder.append("\nThe cas Result was "+_casResponse);
        return builder.toString();
    }
}
