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

package com.dreameddeath.core.dao.view;

import com.couchbase.client.java.view.AsyncViewRow;
import com.couchbase.client.java.view.ViewRow;
import com.dreameddeath.core.couchbase.ICouchbaseBucket;
import com.dreameddeath.core.couchbase.exception.StorageException;
import com.dreameddeath.core.dao.document.CouchbaseDocumentDao;
import com.dreameddeath.core.dao.document.CouchbaseDocumentWithKeyPatternDao;
import com.dreameddeath.core.dao.exception.DaoException;
import com.dreameddeath.core.dao.model.view.*;
import com.dreameddeath.core.dao.model.view.impl.ViewAsyncQueryResult;
import com.dreameddeath.core.dao.model.view.impl.ViewQuery;
import com.dreameddeath.core.dao.model.view.impl.ViewQueryResult;
import com.dreameddeath.core.dao.session.ICouchbaseSession;
import com.dreameddeath.core.model.document.CouchbaseDocument;
import rx.Observable;

/**
 * Created by Christophe Jeunesse on 18/12/2014.
 */
public abstract class CouchbaseViewDao<TKEY,TVALUE,TDOC extends CouchbaseDocument> {
    private CouchbaseDocumentDao<TDOC> _parentDao;
    private String _viewName;
    private String _designDoc;
    public abstract String getContent();

    public IViewQuery<TKEY,TVALUE,TDOC> buildViewQuery(){ return new ViewQuery<TKEY,TVALUE,TDOC>(this);}
    public abstract IViewKeyTranscoder<TKEY> getKeyTranscoder();
    public abstract IViewTranscoder<TVALUE> getValueTranscoder();

    public CouchbaseViewDao(String designDoc,String viewName,CouchbaseDocumentDao<TDOC> parentDao){
        _parentDao = parentDao;
        _designDoc = designDoc;
        _viewName = viewName;
    }

    public String getViewName(){return _viewName;}
    public String getDesignDoc(){return _designDoc;}
    public CouchbaseDocumentDao<TDOC> getParentDao(){return _parentDao;}


    public ICouchbaseBucket getClient(){ return _parentDao.getClient(); }

    public String buildMapString(){
        String bucketPrefix = getClient().getPrefix();
        if(bucketPrefix!=null){
            bucketPrefix=bucketPrefix+ICouchbaseBucket.Utils.KEY_SEP;
        }
        StringBuilder sb = new StringBuilder();
        sb.append("function (doc, meta) {\n");
        //Append Prefix if needed
        if(_parentDao instanceof  CouchbaseDocumentWithKeyPatternDao){
            String pattern = ((bucketPrefix!=null)?bucketPrefix:"")+((CouchbaseDocumentWithKeyPatternDao)_parentDao).getKeyPattern();
            pattern = pattern.replaceAll("([/\\$])","\\\\$1");
            sb.append("if(/^"+pattern + "$/.test(meta.id)==false) return;");
            sb.append("\n");
        }
        else if(bucketPrefix!=null){
            sb.append("if(meta.id.indexOf(\""+bucketPrefix+"\")!==0) return;");
            sb.append("\n");
        }
        String content = getContent();
        if(bucketPrefix!=null) {
            content.replaceAll("meta\\.id","meta.id.replace(/^"+bucketPrefix.replaceAll("([/\\$])","\\\\$1")+"/,\"\")");
        }
        sb.append("\n\n" + content + "\n");
        sb.append("}\n");
        return sb.toString();
    }

    public IViewQueryRow<TKEY,TVALUE,TDOC> map(AsyncViewRow row){
        try{
            final TKEY key = getKeyTranscoder().decode(row.key());
            final TVALUE value = (row.value()!=null)?getValueTranscoder().decode(row.value()):null;
            final String docKey = ICouchbaseBucket.Utils.extractKey(_parentDao.getClient().getPrefix(), row.id());
            return new IViewQueryRow<TKEY, TVALUE, TDOC>() {
                @Override public TKEY getKey() { return key;}
                @Override public TVALUE getValue() { return value;}
                @Override public String getDocKey() {return docKey;}
                @Override public TDOC getDoc(ICouchbaseSession session) throws DaoException, StorageException {return (TDOC)session.get(docKey);}
            };
        }
        catch(Exception e){
            throw new RuntimeException("Decoding exception",e);
        }
    }

    public IViewQueryRow<TKEY,TVALUE,TDOC> map(ViewRow row){
        try{
            final TKEY key = getKeyTranscoder().decode(row.key());
            final TVALUE value = (row.value()!=null)?getValueTranscoder().decode(row.value()):null;
            final String docKey = ICouchbaseBucket.Utils.extractKey(_parentDao.getClient().getPrefix(), row.id());
            return new IViewQueryRow<TKEY, TVALUE, TDOC>() {
                @Override public TKEY getKey() { return key;}
                @Override public TVALUE getValue() { return value;}
                @Override public String getDocKey() {return docKey;}
                @Override public TDOC getDoc(ICouchbaseSession session) throws DaoException, StorageException {return (TDOC)session.get(docKey);}
            };
        }
        catch(Exception e){
            throw new RuntimeException("Decoding exception",e);
        }
    }

    public IViewQueryResult query(ICouchbaseSession session,boolean isCalcOnly,IViewQuery query){
        return ViewQueryResult.from(query,getClient().query(query.toCouchbaseQuery()));
    }

    public Observable<IViewAsyncQueryResult> asyncQuery(ICouchbaseSession session,boolean isCalcOnly,IViewQuery query){
        return getClient().asyncQuery(query.toCouchbaseQuery()).map(asyncView -> ViewAsyncQueryResult.from(query, asyncView));
    }

}

