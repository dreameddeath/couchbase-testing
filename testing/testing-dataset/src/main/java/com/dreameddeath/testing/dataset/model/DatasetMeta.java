package com.dreameddeath.testing.dataset.model;

import org.joda.time.DateTime;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Created by Christophe Jeunesse on 12/04/2016.
 */
public class DatasetMeta {
    private Dataset parent=null;
    private DatasetElement parentElement=null;
    private Type type=Type.UNKNOWN;
    private String path=null;
    private String name=null;
    private List<DatasetValue> params=new ArrayList<>();

    public void setName(String name){
        this.name=name;
        this.type = Type.getFromName(name);
    }

    public String getName(){
        return name;
    }

    public void prepare(Dataset parent, DatasetElement parentElement, String path) {
        this.parent = parent;
        this.parentElement = parentElement;
        this.path = path;
        if(this.getType()==Type.DECLARE_VAR){
            String name = this.getParam(0,String.class);
            String varType = this.getParam(1,String.class);
            if("string".equalsIgnoreCase(varType)){
                parent.getParserContext().addVariable(name,String.class);
            }
            else if("integer".equalsIgnoreCase(varType)){
                parent.getParserContext().addVariable(name,Integer.class);
            }
            else if("long".equalsIgnoreCase(varType)){
                parent.getParserContext().addVariable(name,Long.class);
            }
            else if("decimal".equalsIgnoreCase(varType)){
                parent.getParserContext().addVariable(name,BigDecimal.class);
            }
            else if("datetime".equalsIgnoreCase(varType)){
                parent.getParserContext().addVariable(name,DateTime.class);
            }
            else if("boolean".equalsIgnoreCase(varType)){
                parent.getParserContext().addVariable(name,Boolean.class);
            }
            else if("map".equalsIgnoreCase(varType)){
                parent.getParserContext().addVariable(name,Map.class);
            }
            else if("list".equalsIgnoreCase(varType)){
                parent.getParserContext().addVariable(name,List.class);
            }
            else{
                try {
                    parent.getParserContext().addVariable(name, this.getClass().getClassLoader().loadClass(varType));
                }
                catch(ClassNotFoundException e){
                    throw new RuntimeException("Cannot find class "+varType);
                }
            }
        }
    }

    public Type getType() {
        return type;
    }

    public void addParam(DatasetValue value){
        this.params.add(value);
    }

    public List<DatasetValue> getParams(){
        return Collections.unmodifiableList(params);
    }

    public <T> T getParam(int pos,Class<T> clazz){
        return params.get(pos).getContent(clazz);
    }

    public enum Type{
        INIT_FROM("InitFrom"),
        NOT_NULL("NotNull"),
        NOT_EXISTING("NotExisting"),
        CONTAINS("Contains"),
        MATCH("Match"),
        NB_VALUES("NbValues"),
        LT("LowerThan"),
        LTE("LowerThanOrEqual"),
        GT("HigherThan"),
        GTE("HigherThanOrEqual"),
        TYPE("Type"),
        EVAL("Eval"),
        DECLARE_VAR("Declare"),
        FOR_BUILD_ONLY("BuildOnly"),
        UNKNOWN("");

        private String name;

        Type(String name){
            this.name= name;
        }

        public String getName(){
            return name;
        }

        public static Type getFromName(String name){
            for(Type type:Type.values()){
                if(type.name.equalsIgnoreCase(name)){
                    return type;
                }
            }
            return UNKNOWN;
        }
    }
}
