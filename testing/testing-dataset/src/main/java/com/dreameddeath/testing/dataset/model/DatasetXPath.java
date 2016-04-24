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

package com.dreameddeath.testing.dataset.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by Christophe Jeunesse on 08/12/2014.
 */
public class DatasetXPath {
    private Dataset parent = null;
    private DatasetElement parentElement=null;
    private String path=null;

    private List<DatasetMeta> metas=new ArrayList<>();
    private List<DatasetXPathPart> parts=new ArrayList<>();


    public void addMeta(DatasetMeta meta){
        metas.add(meta);
    }

    public void addPart(DatasetXPathPart part){
        parts.add(part);
    }

    public List<DatasetXPathPart> getParts(){
        return Collections.unmodifiableList(parts);
    }

    public List<DatasetMeta> getMetas(){
        return Collections.unmodifiableList(metas);
    }

    public String getPath(){
        StringBuilder sb = new StringBuilder();

        for(DatasetXPathPart part:parts){
            if(sb.length()>0 && (part.getType()!= DatasetXPathPart.PartType.ARRAY_RANGE)) sb.append(".");
            sb.append(part.getPath());
        }
        return sb.toString();
    }

    public String toString(){
        return getPath();
    }

    public void prepare(Dataset parent, DatasetElement parentElt, String path) {
        this.parent =parent;
        this.parentElement = parentElt;
        this.path = path;
        StringBuilder subPathBuilder=new StringBuilder();

        for(DatasetMeta meta:metas){
            meta.prepare(parent,parentElement,path);
        }

        for(DatasetXPathPart part:parts){
            part.prepare(parent,parentElt,path,subPathBuilder.toString());
            if(subPathBuilder.length()!=0){
                subPathBuilder.append('.');
            }
            subPathBuilder.append(part.getPath());
        }
    }

    public enum PartType{
        STD,
        MATCH_ALL,
        MATCH_ALL_RECURSIVE,
        MATCH_ALL_PREDICATE
    }
}
