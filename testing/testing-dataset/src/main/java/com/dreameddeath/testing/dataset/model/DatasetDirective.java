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

import com.google.common.base.Preconditions;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by Christophe Jeunesse on 15/04/2016.
 */
public class DatasetDirective {
    private Type type;
    private List<DatasetValue> params=new ArrayList<>();
    private Dataset parent;
    private DatasetElement parentElement;
    private Dataset importedDataset=null;

    public void setName(String name){
        this.type = Type.getFromName(name);
        Preconditions.checkNotNull(type,"The directive <%s> isn't managed",name);
    }

    public void addParam(DatasetValue param){
        this.params.add(param);
    }

    public List<DatasetValue> getParams() {
        return Collections.unmodifiableList(params);
    }

    public <T> T getParam(int pos,Class<T> clazz){
        return params.get(pos).getContent(clazz);
    }

    public Type getType() {
        return type;
    }

    public void prepare(Dataset parent, DatasetElement datasetElement) {
        this.parent=parent;
        this.parentElement=datasetElement;
        switch (type){
            case IMPORT:
                this.importedDataset = this.parent.getManager().getDatasetByName(getParam(0,String.class));
                Preconditions.checkNotNull(importedDataset,"Cannot find the referenced dataset %s",params.get(0));
                break;
            default:
                //Nothing to do;
        }
    }

    public Dataset getImportedDataset() {
        return importedDataset;
    }

    public enum Type{
        DATASET_NAME("dataset_name"),
        DATASET_ELT_NAME("dataset_elt_name"),
        IMPORT("import")
        ;
        private String name;

        Type(String name){
            this.name = name;
        }

        public static Type getFromName(String name){
            for(Type currType:Type.values()){
                if(currType.name.equalsIgnoreCase(name)){
                    return currType;
                }
            }
            return null;
        }
    }

    public DatasetElement getParentElement() {
        return parentElement;
    }

    public Dataset getParent() {
        return parent;
    }
}
