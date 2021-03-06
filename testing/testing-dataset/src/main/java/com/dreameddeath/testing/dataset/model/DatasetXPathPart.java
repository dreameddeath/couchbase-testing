package com.dreameddeath.testing.dataset.model;

import com.dreameddeath.testing.dataset.utils.DatasetUtils;

/**
 * Created by Christophe Jeunesse on 12/04/2016.
 */
public class DatasetXPathPart {
    private Dataset parent=null;
    private DatasetElement parentElement=null;
    private String path=null;
    private String subPath=null;

    private PartType type;
    private String localName=null;
    private DatasetRange range =null;
    private DatasetMvel mvel=null;

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
            type = PartType.FIELD_NAME;
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
        this.type=PartType.ARRAY_RANGE;
    }

    public void setMvel(DatasetMvel mvel){
        this.mvel=mvel;
        this.type=PartType.MVEL;
    }

    public String getLocalName() {
        return localName;
    }

    public String getPath(){
        StringBuilder sb = new StringBuilder();
        if(type==PartType.ARRAY_RANGE){
            sb.append(range.getPathString());
        }
        else if(type==PartType.MVEL){
            sb.append("$mvel$");
        }
        else{
            sb.append(localName);
        }
        return sb.toString();
    }

    public PartType getType() {
        return type;
    }

    public DatasetRange getRange() {
        return range;
    }

    public void prepare(Dataset parent, DatasetElement parentElt, String path, String subPath) {
        this.parent=parent;
        this.parentElement=parentElt;
        this.path = path;
        this.subPath=subPath;
        switch(type){
            case MVEL:
                mvel.prepare(parent,parentElt);
                break;
            case PREDICATE:
                //TODO
                break;
            default:
                //Nothing to do
        }
    }

    public DatasetMvel getMvel() {
        return mvel;
    }

    public enum PartType{
        ARRAY_RANGE,
        PREDICATE,
        FIELD_NAME,
        MVEL,
        MATCH_ALL,
        MATCH_ALL_RECURSIVE
    }
}
