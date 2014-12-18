package com.dreameddeath.core.dao.view;

import com.dreameddeath.core.dao.document.CouchbaseDocumentWithKeyPatternDao;

import java.util.Collections;
import java.util.List;

/**
 * Created by ceaj8230 on 18/12/2014.
 */
public abstract class CouchbaseViewDao {
    public abstract String getViewDesignDocumentName();
    public abstract String getViewName();

    public List<CouchbaseDocumentWithKeyPatternDao> filteringViewForList(){return Collections.emptyList();}

    public abstract String getContent();

    public String buildViewString(String viewPrefix){
        StringBuilder sb = new StringBuilder();
        sb.append("function (doc, meta) {\n");
        if(viewPrefix!=null){
            sb.append("\t");
            sb.append("if(meta.id.indexOf("+viewPrefix+")==0) return;");
            sb.append("\n");
        }

        for(CouchbaseDocumentWithKeyPatternDao dao:filteringViewForList()){
            sb.append("\t");
            sb.append("if(/^"+((viewPrefix!=null)?viewPrefix:"")+dao.getKeyPattern()+"$/.test(meta.id)) return;");
            sb.append("\n");
        }

        sb.append("\n\n"+getContent()+"\n");
        sb.append("\n");

        return sb.toString();
    }
}
