package com.dreameddeath.core.dao;

import com.dreameddeath.core.dao.counter.CouchbaseCounterDaoFactory;
import com.dreameddeath.core.dao.document.CouchbaseDocumentDaoFactory;


/**
 * Created by ceaj8230 on 02/09/2014.
 */
public class CouchbaseSessionFactory {
    private CouchbaseDocumentDaoFactory _documentDaoFactory;
    private CouchbaseCounterDaoFactory _counterDaoFactory;

    public CouchbaseSessionFactory(CouchbaseDocumentDaoFactory docDaoFactory,CouchbaseCounterDaoFactory counterDaoFactory){
        _documentDaoFactory =  docDaoFactory;
        _counterDaoFactory = counterDaoFactory;
    }

    public CouchbaseSession newReadOnlySession(){
        return new CouchbaseSession(_documentDaoFactory,_counterDaoFactory, CouchbaseSession.SessionType.READ_ONLY);
    }

    public CouchbaseSession newCalcOnlySession(){
        return new CouchbaseSession(_documentDaoFactory,_counterDaoFactory, CouchbaseSession.SessionType.CALC_ONLY);
    }

    public CouchbaseSession newReadWriteSession(){
        return new CouchbaseSession(_documentDaoFactory,_counterDaoFactory, CouchbaseSession.SessionType.READ_WRITE);
    }


}
