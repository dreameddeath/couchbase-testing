package com.dreameddeath.core.dao.counter;

import com.dreameddeath.core.exception.dao.DaoNotFoundException;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;

/**
 * Created by ceaj8230 on 02/09/2014.
 */
public class CouchbaseCounterDaoFactory {
    private Map<Pattern,CouchbaseCounterDao> _patternsMap
            = new ConcurrentHashMap<Pattern,CouchbaseCounterDao>();

    public void addDao(CouchbaseCounterDao dao){
        _patternsMap.put(Pattern.compile("^"+dao.getKeyPattern()+"$"),dao);
    }

    public CouchbaseCounterDao getDaoForKey(String key) throws DaoNotFoundException {
        for(Pattern pattern:_patternsMap.keySet()){
            if(pattern.matcher(key).matches()){
                return _patternsMap.get(pattern);
            }
        }
        throw new DaoNotFoundException(key, DaoNotFoundException.Type.COUNTER);
    }

}
