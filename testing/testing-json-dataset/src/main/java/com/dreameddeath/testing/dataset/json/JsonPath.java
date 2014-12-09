package com.dreameddeath.testing.dataset.json;

import com.fasterxml.jackson.databind.JsonNode;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by ceaj8230 on 08/12/2014.
 */
public class JsonPath {
    private String _path;
    private PartType _type;
    private String _localName;
    private String _subPath;
    private JsonPath _subJsonPath;

    protected void parsePath(){
        if(_path.contains(".")){
            _localName = _path.substring(0,_path.indexOf('.'));
            _subPath = _path.substring(_path.indexOf('.')+1);
        }
        else{
            _localName = _path;
            _subPath = null;
            _subJsonPath = null;
        }
    }

    public List<JsonNode> getMatchingNodes(JsonNode node){
        List<JsonNode> result = new ArrayList<>();
        if(node.isArray()){

        }
        else if(node.isObject()){

        }
        return result;
    }

    public String getPath(){return _path;}
    public void setPath(String path){ _path=path; parsePath();}

    public enum PartType{
        STD,
        MATCH_ALL,
        MATCH_ALL_RECURSIVE,
        MATCH_ALL_PREDICATE
    }
}
