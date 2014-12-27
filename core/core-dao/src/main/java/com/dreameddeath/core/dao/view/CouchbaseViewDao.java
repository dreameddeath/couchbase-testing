package com.dreameddeath.core.dao.view;

import com.couchbase.client.java.view.ViewResult;
import com.couchbase.client.java.view.ViewRow;
import com.dreameddeath.core.dao.document.CouchbaseDocumentDao;
import com.dreameddeath.core.dao.document.CouchbaseDocumentWithKeyPatternDao;
import com.dreameddeath.core.model.document.CouchbaseDocument;
import com.dreameddeath.core.model.view.IViewQuery;
import com.dreameddeath.core.model.view.IViewQueryResult;
import com.dreameddeath.core.model.view.IViewValueTranscoder;
import com.dreameddeath.core.session.ICouchbaseSession;
import com.dreameddeath.core.storage.ICouchbaseBucket;

import java.util.List;

/**
 * Created by ceaj8230 on 18/12/2014.
 */
public abstract class CouchbaseViewDao<TKEY,TVALUE,TDOC extends CouchbaseDocument> {
    private CouchbaseDocumentDao<TDOC> _parentDao;
    private String _viewName;
    private String _designDoc;

    public abstract String getContent();
    public abstract IViewQuery<TKEY,TVALUE,TDOC> buildViewQuery();
    public abstract IViewValueTranscoder<TVALUE> getValueTranscoder();

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

        sb.append("\n\n"+getContent()+"\n");
        sb.append("}\n");

        return sb.toString();
    }


    public IViewQueryResult query(ICouchbaseSession session,boolean isCalcOnly,IViewQuery query){
        ViewResult result = getClient().query(query.toCouchbaseQuery());
        List<ViewRow> list = result.allRows();
        return null;
    }


}

