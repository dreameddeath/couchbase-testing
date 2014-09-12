package com.dreameddeath.core.dao;

import com.dreameddeath.core.dao.counter.CouchbaseCounterDaoFactory;
import com.dreameddeath.core.dao.document.CouchbaseDocumentDao;
import com.dreameddeath.core.dao.document.CouchbaseDocumentDaoFactory;
import com.dreameddeath.core.dao.unique.CouchbaseUniqueKeyDaoFactory;
import com.dreameddeath.core.user.User;


/**
 * Created by ceaj8230 on 02/09/2014.
 */
public class CouchbaseSessionFactory {
    private final CouchbaseDocumentDaoFactory _documentDaoFactory;
    private final CouchbaseCounterDaoFactory _counterDaoFactory;
    private final CouchbaseUniqueKeyDaoFactory _uniqueKeyDaoFactory;

    public CouchbaseSessionFactory(CouchbaseDocumentDaoFactory docDaoFactory,CouchbaseCounterDaoFactory counterDaoFactory,CouchbaseUniqueKeyDaoFactory uniqueDaoFactory){
        _documentDaoFactory =  docDaoFactory;
        _counterDaoFactory = counterDaoFactory;
        _uniqueKeyDaoFactory = uniqueDaoFactory;
    }

    public CouchbaseDocumentDaoFactory getDocumentDaoFactory(){ return _documentDaoFactory;}
    public CouchbaseCounterDaoFactory getCounterDaoFactory(){ return _counterDaoFactory;}
    public CouchbaseUniqueKeyDaoFactory getUniqueKeyDaoFactory(){ return _uniqueKeyDaoFactory;}

    public CouchbaseSession newSession(CouchbaseSession.SessionType type, User user){
        return new CouchbaseSession(this,type,user);
    }

    public CouchbaseSession newReadOnlySession(User user){return newSession(CouchbaseSession.SessionType.READ_ONLY,user);}
    public CouchbaseSession newReadWriteSession(User user){return newSession(CouchbaseSession.SessionType.READ_WRITE,user);}
    public CouchbaseSession newCalcOnlySession(User user){return newSession(CouchbaseSession.SessionType.CALC_ONLY,user);}

}
