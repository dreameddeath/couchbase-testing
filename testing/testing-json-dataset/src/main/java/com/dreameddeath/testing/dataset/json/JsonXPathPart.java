package com.dreameddeath.testing.dataset.json;

/**
 * Created by Christophe Jeunesse on 12/04/2016.
 */
public class JsonXPathPart {
    private PartType type;
    private String localName;
    private JsonOffset offset=null;

    public void setLocalName(String name){
        this.localName = name;
        if("*".equals(localName)){
            type=PartType.MATCH_ALL;
        }
        else if("**".equals(localName)){
            type = PartType.MATCH_ALL_RECURSIVE;
        }
        else{
            type = PartType.STD;
            if(localName.startsWith("\"")){
                this.localName = localName.substring(1,localName.length()-1)
                        .replaceAll("\\\\(.)","$1");
            }
        }
    }

    public void setOffset(JsonOffset offset){
        this.offset = offset;
    }

    public String getPath(){
        StringBuilder sb = new StringBuilder();
        sb.append(localName);
        if(this.offset!=null){
            sb.append(offset.getPathString());
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
