package com.dreameddeath.testing.dataset.model;

import com.dreameddeath.testing.dataset.utils.DatasetUtils;

/**
 * Created by Christophe Jeunesse on 12/04/2016.
 */
public class DatasetXPathPart {
    private PartType type;
    private String localName;
    private DatasetRange range =null;

    public void setLocalName(String name){
        if("*".equals(name)){
            type=PartType.MATCH_ALL;
            this.localName = "*";
        }
        else if("**".equals(name)){
            type = PartType.MATCH_ALL_RECURSIVE;
            this.localName="**";
        }
        else{
            type = PartType.STD;
            if(name.startsWith("\"")){
                this.localName = DatasetUtils.parseJavaEncodedString(name);
            }
            else{
                this.localName=name;
            }
        }
    }

    public void setRange(DatasetRange range){
        this.range = range;
    }

    public String getPath(){
        StringBuilder sb = new StringBuilder();
        sb.append(localName);
        if(this.range !=null){
            sb.append(range.getPathString());
        }
        return sb.toString();
    }

    public enum PartType{
        STD,
        MATCH_ALL,
        MATCH_ALL_RECURSIVE,
        MATCH_ALL_PREDICATE
    }
}
