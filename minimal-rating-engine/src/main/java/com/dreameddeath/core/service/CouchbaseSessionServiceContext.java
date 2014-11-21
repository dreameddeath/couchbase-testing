package com.dreameddeath.core.service;

import com.dreameddeath.core.CouchbaseSession;
import com.dreameddeath.core.model.common.BaseCouchbaseDocument;
import org.joda.time.DateTime;

/**
 * Created by ceaj8230 on 03/11/2014.
 */
public class CouchbaseSessionServiceContext {
    final private CouchbaseSession _session;

    public CouchbaseSessionServiceContext(CouchbaseSession session){
        _session = session;
    }

    public CouchbaseSession getSession(){return _session;}

    public <T extends BaseCouchbaseDocument> T newEntity(Class<T> clazz){
        return _session.newEntity(clazz);
    }

    public DateTime getCurrentDate(){
        return _session.getDateTimeService().getCurrentDate();
    }
}
