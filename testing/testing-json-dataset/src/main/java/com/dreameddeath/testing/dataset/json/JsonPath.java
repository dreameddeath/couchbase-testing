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

package com.dreameddeath.testing.dataset.json;

import com.fasterxml.jackson.databind.JsonNode;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Christophe Jeunesse on 08/12/2014.
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
