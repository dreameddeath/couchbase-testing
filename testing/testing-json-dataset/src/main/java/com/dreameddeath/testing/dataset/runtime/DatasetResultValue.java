package com.dreameddeath.testing.dataset.runtime;

import com.dreameddeath.testing.dataset.model.DatasetValue;
import com.fasterxml.jackson.annotation.JsonValue;
import org.joda.time.DateTime;

import java.math.BigDecimal;

/**
 * Created by Christophe Jeunesse on 17/04/2016.
 */
public class DatasetResultValue {
    private DatasetValue.Type type=null;
    private DatasetResultObject objVal;
    private DatasetResultArray arrayVal;
    private DateTime dateTimeVal;
    private String strVal;
    private Long longVal;
    private BigDecimal decimalVal;
    private Boolean boolVal;


    public DatasetValue.Type getType() {
        return type;
    }

    public DatasetResultObject getObject() {
        return objVal;
    }

    public DatasetResultArray getArrayVal() {
        return arrayVal;
    }

    public boolean isNew(){
        return type==null;
    }

    public void setObject(DatasetResultObject obj){
        this.objVal = obj;
        this.type = DatasetValue.Type.OBJECT;
    }

    public void setArray(DatasetResultArray array) {
        this.arrayVal = array;
        this.type = DatasetValue.Type.ARRAY;
    }


    public void setDecimal(BigDecimal decimal) {
        this.decimalVal = decimal;
        this.type = DatasetValue.Type.DECIMAL;
    }

    public void setStr(String strVal) {
        this.strVal = strVal;
        this.type = DatasetValue.Type.STRING;
    }

    public void setLong(Long longVal) {
        this.longVal = longVal;
        this.type = DatasetValue.Type.LONG;
    }

    public void setBool(Boolean boolVal) {
        this.boolVal = boolVal;
        this.type = DatasetValue.Type.BOOL;
    }

    public void setDateTime(DateTime dateTimeVal) {
        this.dateTimeVal = dateTimeVal;
        this.type = DatasetValue.Type.DATETIME;
    }

    public void setValue(DatasetResultValue value){
        this.type=value.type;
        this.objVal = value.objVal;
        this.arrayVal=value.arrayVal;
        this.decimalVal =value.decimalVal;
        this.longVal = value.longVal;
        this.boolVal = value.boolVal;
        this.dateTimeVal = value.dateTimeVal;
        this.strVal = value.strVal;
    }


    @JsonValue
    public Object getContent(){
        if(type==null){
            return null;
        }
        switch(type){
            case OBJECT: return objVal;
            case ARRAY: return arrayVal;
            case DECIMAL: return decimalVal;
            case LONG: return longVal;
            case DATETIME:return dateTimeVal;
            case STRING: return strVal;
            case BOOL: return boolVal;
            default:
                throw new RuntimeException("not managed type "+type);
        }
    }
}
