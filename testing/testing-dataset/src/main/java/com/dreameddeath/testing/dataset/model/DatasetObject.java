/*
 *
 *  * Copyright Christophe Jeunesse
 *  *
 *  *    Licensed under the Apache License, Version 2.0 (the "License");
 *  *    you may not use this file except in compliance with the License.
 *  *    You may obtain a copy of the License at
 *  *
 *  *      http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  *    Unless required by applicable law or agreed to in writing, software
 *  *    distributed under the License is distributed on an "AS IS" BASIS,
 *  *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  *    See the License for the specific language governing permissions and
 *  *    limitations under the License.
 *
 */

package com.dreameddeath.testing.dataset.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by Christophe Jeunesse on 14/04/2016.
 */
public class DatasetObject {
    private String path=null;
    private final List<DatasetObjectNode> nodes = new ArrayList<>();

    public void addNode(DatasetObjectNode node){
        nodes.add(node);
    }

    public List<DatasetObjectNode> getNodes(){
        return Collections.unmodifiableList(nodes);
    }

    public void prepare(Dataset parent,DatasetElement parentElt){
        this.prepare(parent,parentElt,"");
    }

    public void prepare(final Dataset parent,final DatasetElement parentElt,final String path){
        nodes.forEach(node->node.prepare(parent,parentElt,path));
    }

    public String getFullPath(){
        return this.path;
    }
}
