package com.dreameddeath.core;

import com.dreameddeath.core.dao.document.BaseCouchbaseDocumentDaoFactory;
import com.dreameddeath.core.dao.counter.CouchbaseCounterDaoFactory;
import com.dreameddeath.core.dao.unique.CouchbaseUniqueKeyDaoFactory;
import com.dreameddeath.core.date.DateTimeServiceFactory;
import com.dreameddeath.core.user.User;


/**
 * Created by ceaj8230 on 02/09/2014.
 */
public class CouchbaseSessionFactory {
    private final BaseCouchbaseDocumentDaoFactory _documentDaoFactory;
    private final CouchbaseCounterDaoFactory _counterDaoFactory;
    private final CouchbaseUniqueKeyDaoFactory _uniqueKeyDaoFactory;
    private final DateTimeServiceFactory _dateTimeServiceFactory;

    public CouchbaseSessionFactory(BaseCouchbaseDocumentDaoFactory docDaoFactory,CouchbaseCounterDaoFactory counterDaoFactory,CouchbaseUniqueKeyDaoFactory uniqueDaoFactory,DateTimeServiceFactory dateTimeServiceFactory){
        _documentDaoFactory =  docDaoFactory;
        _counterDaoFactory = counterDaoFactory;
        _uniqueKeyDaoFactory = uniqueDaoFactory;
        _dateTimeServiceFactory = dateTimeServiceFactory;
    }

    public BaseCouchbaseDocumentDaoFactory getDocumentDaoFactory(){ return _documentDaoFactory;}
    public CouchbaseCounterDaoFactory getCounterDaoFactory(){ return _counterDaoFactory;}
    public CouchbaseUniqueKeyDaoFactory getUniqueKeyDaoFactory(){ return _uniqueKeyDaoFactory;}
    public DateTimeServiceFactory getDateTimeServiceFactory(){ return _dateTimeServiceFactory;}

    public CouchbaseSession newSession(CouchbaseSession.SessionType type, User user){
        return new CouchbaseSession(this,type,user);
    }

    public CouchbaseSession newReadOnlySession(User user){return newSession(CouchbaseSession.SessionType.READ_ONLY,user);}
    public CouchbaseSession newReadWriteSession(User user){return newSession(CouchbaseSession.SessionType.READ_WRITE,user);}
    public CouchbaseSession newCalcOnlySession(User user){return newSession(CouchbaseSession.SessionType.CALC_ONLY,user);}

}
