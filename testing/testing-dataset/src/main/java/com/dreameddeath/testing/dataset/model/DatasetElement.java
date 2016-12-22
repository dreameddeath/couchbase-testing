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
public class DatasetElement {
    private String name;
    private Type type;
    private List<DatasetMeta> metaList=new ArrayList<>();
    private DatasetMvel mvelElement;
    private DatasetDirective directive;
    private final DatasetValue value=new DatasetValue();
    private Dataset parent;
    //private List<DatasetValue> arrayElt=new ArrayList<>();
    //private DatasetObject objElt;

    public void setName(String name) {
        this.name = name;
    }

    public void setMvel(DatasetMvel mvelElement) {
        this.mvelElement = mvelElement;
        this.type = Type.MVEL;
    }

    public void setObject(DatasetObject objElt) {
        this.value.setObjectValue(objElt);
        this.type =Type.OBJECT;
    }

    public void setArray(List<DatasetValue> arrayElt) {
        this.value.setArrayValue(arrayElt);
        this.type = Type.ARRAY;
    }

    public void addMeta(DatasetMeta meta){
        this.metaList.add(meta);
    }


    public void setDirective(DatasetDirective directive){
        this.directive=directive;
        this.type = Type.DIRECTIVE;
    }

    public Object getContent(){
        switch (type){
            case OBJECT:return this.value.getObjVal();
            case ARRAY: return this.value.getArrayVal();
            case MVEL:return this.mvelElement;
            case DIRECTIVE:return this.directive;
            default : return null;
        }
    }

    public void prepare(Dataset parent){
        this.parent=parent;
        for (DatasetMeta datasetMeta : metaList) {
            datasetMeta.prepare(parent,this,"");
        }

        switch (type){
            case OBJECT:case ARRAY:
                this.value.prepare(parent,this,"");
                break;
            case MVEL:this.mvelElement.prepare(parent,this);break;
            case DIRECTIVE:this.directive.prepare(parent,this);break;
        }
    }

    public String getName() {
        return name;
    }

    public Type getType() {
        return type;
    }

    public List<DatasetMeta> getMetaList() {
        return Collections.unmodifiableList(metaList);
    }

    public DatasetMvel getMvel() {
        return mvelElement;
    }

    public DatasetObject getObject() {
        return value.getObjVal();
    }

    public List<DatasetValue> getArray() {
        return value.getArrayVal();
    }

    public DatasetValue getValue(){
        return value;
    }

    public boolean isValue(){
        return type==Type.OBJECT||type==Type.ARRAY;
    }
    public DatasetDirective getDirective(){
        return directive;
    }

    public Dataset getDataset() {
        return parent;
    }

    public enum Type{
        OBJECT,
        ARRAY,
        MVEL,
        DIRECTIVE
    }

    public boolean hasMeta(DatasetMeta.Type type){
        return metaList.stream().filter(meta->meta.getType()==type).count()>0;
    }
}
