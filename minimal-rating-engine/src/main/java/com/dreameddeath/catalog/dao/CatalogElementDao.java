/*
 * Copyright Christophe Jeunesse
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.dreameddeath.catalog.dao;

import com.dreameddeath.catalog.model.CatalogElement;
import com.dreameddeath.catalog.model.CatalogItemVersion;
import com.dreameddeath.core.couchbase.CouchbaseBucketWrapper;
import com.dreameddeath.core.dao.business.CouchbaseDocumentDaoWithUID;
import com.dreameddeath.core.dao.counter.CouchbaseCounterDao;
import com.dreameddeath.core.dao.document.BaseCouchbaseDocumentDaoFactory;
import com.dreameddeath.core.dao.exception.dao.DaoException;

/**
 * Created by Christophe Jeunesse on 06/09/2014.
 */
public abstract class CatalogElementDao<T extends CatalogElement> extends CouchbaseDocumentDaoWithUID<T> {
    public static final String CAT_ELEMENT_CNT_KEY_FMT_PATTERN="cat/%s/cnt";
    public static final String CAT_ELEMENT_FMT_KEY="cat/%s/%s/%d.%d.%d";
    public static final String CAT_ELEMENT_FMT_KEY_PATTERN="cat/%s/(\\w+)/\\d+.\\d+.\\d+";

    private final String _keyUidFmt;
    private final String _cntKey;
    private final String _keyDomain;
    private final String _keyPattern;

    public abstract String getKeyDomain();
    protected String getDefaultKeyUidFmt(){return "%10d";}

    public CatalogElementDao(CouchbaseBucketWrapper client,BaseCouchbaseDocumentDaoFactory factory){
        super(client,factory);
        _keyDomain = getKeyDomain();
        _keyUidFmt = getDefaultKeyUidFmt();
        _keyPattern = String.format(CAT_ELEMENT_FMT_KEY_PATTERN, getKeyDomain());
        _cntKey=String.format(CAT_ELEMENT_CNT_KEY_FMT_PATTERN, getKeyDomain());
        registerCounterDao(new CouchbaseCounterDao.Builder().withKeyPattern(
                _cntKey
            ).withDefaultValue(1L)
        );
    }

    @Override
    final public void buildKey(T obj) throws DaoException {
        //Uid generation by default
        if(obj.getUid()==null){
            obj.setUid(String.format(_keyUidFmt,obj.getBaseMeta().getSession().incrCounter(_cntKey,1)));
        }
        if(obj.getVersion()==null){ obj.setVersion(new CatalogItemVersion());}
        //Normalize version
        CatalogItemVersion currVersion= obj.getVersion();
        if(currVersion.getMajor()==null){currVersion.setMajor(1);}
        if(currVersion.getMinor()==null){currVersion.setMinor(0);}
        if(currVersion.getPatch()==null){currVersion.setPatch(0);}

        obj.getBaseMeta().setKey(String.format(
                        CAT_ELEMENT_FMT_KEY,
                        _keyDomain,
                        obj.getUid(),
                        currVersion.getMajor(), currVersion.getMinor(), currVersion.getPatch()
                )
        );
    }

    @Override
    final public String getKeyPattern(){ return _keyPattern; }

    @Override
    final public String getKeyFromUID(String uid){return String.format(
            CAT_ELEMENT_FMT_KEY,
            _keyDomain,
            uid,
            1,0,0);
    }
}
