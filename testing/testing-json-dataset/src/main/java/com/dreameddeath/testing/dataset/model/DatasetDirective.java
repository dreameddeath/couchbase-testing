package com.dreameddeath.testing.dataset.model;

import com.dreameddeath.testing.dataset.utils.DatasetUtils;
import com.google.common.base.Preconditions;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by Christophe Jeunesse on 15/04/2016.
 */
public class DatasetDirective {
    private Type type;
    private List<String> params=new ArrayList<>();
    private Dataset importedDataset=null;

    public void setName(String name){
        this.type = Type.getFromName(name);
        Preconditions.checkNotNull(type,"The directive <%s> isn't managed",name);
    }

    public void addParam(String param){
        this.params.add(DatasetUtils.parseJavaEncodedString(param));
    }

    public List<String> getParams() {
        return Collections.unmodifiableList(params);
    }

    public Type getType() {
        return type;
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
}
