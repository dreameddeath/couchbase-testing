package com.dreameddeath.testing.dataset.model;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


/**
 * Created by Christophe Jeunesse on 13/04/2016.
 */
public class DatasetValue {
    private Type type;
    private List<DatasetMeta> metaList=new ArrayList<>();
    private String strValue;
    private BigDecimal decimalVal;
    private Long longVal;
    private DatasetObject objVal;
    private List<DatasetValue> arrayVal = new ArrayList<>();
    private Boolean boolVal;
    public void setStrValue(String value){
        type = Type.STRING;
        strValue = value;
    }

    public void addMeta(DatasetMeta meta){
        this.metaList.add(meta);
    }

    public void setDecimalValue(String value){
        type = Type.DECIMAL;
        decimalVal = new BigDecimal(value);
    }

    public void setLongValue(String value){
        type = Type.LONG;
        longVal = Long.parseLong(value);
    }

    public void setObjectValue(DatasetObject object){
        this.objVal=object;
        type=Type.OBJECT;
    }

    public void setArrayValue(List<DatasetValue> value){
        this.arrayVal.clear();
        this.arrayVal.addAll(value);
        this.type = Type.ARRAY;
    }

    public void setBool(boolean value){
        this.boolVal = value;
        this.type = Type.BOOL;
    }

    public void setNull(){
        this.type = Type.NULL;
    }

    public Type getType() {
        return type;
    }

    public Object getContent(){
        switch (type){
            case STRING:return strValue;
            case DECIMAL:return decimalVal;
            case LONG:return longVal;
            case OBJECT:return objVal;
            case ARRAY:return Collections.unmodifiableList(arrayVal);
            case BOOL:return boolVal;
            case NULL:return null;
            default:return null;
        }
    }

    public enum Type{
        STRING,
        DECIMAL,
        LONG,
        OBJECT,
        ARRAY,
        BOOL,
        NULL
    }
}
