package com.dreameddeath.testing.dataset.runtime.model;

import com.dreameddeath.testing.dataset.model.DatasetValue;
import com.fasterxml.jackson.annotation.JsonValue;
import org.joda.time.DateTime;

import java.math.BigDecimal;

/**
 * Created by Christophe Jeunesse on 17/04/2016.
 */
public class DatasetResultValue implements Cloneable {
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

    public boolean isComparable(){
        return type== DatasetValue.Type.STRING||
                type== DatasetValue.Type.LONG||
                type== DatasetValue.Type.DATETIME||
                type== DatasetValue.Type.DECIMAL;
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

    public <T> T getContent(Class<T> content){
        return (T) getContent();
    }

    @Override
    protected DatasetResultValue clone(){
        DatasetResultValue result = new DatasetResultValue();
        if(this.type==null){
            return result;
        }
        result.type = this.type;
        switch(type){
            case OBJECT: result.objVal=this.objVal.clone();break;
            case ARRAY: result.arrayVal = this.arrayVal.clone();break;
            case DECIMAL: result.decimalVal=this.decimalVal;break;
            case LONG: result.longVal=this.longVal;break;
            case DATETIME:result.dateTimeVal=this.dateTimeVal;break;
            case STRING: result.strVal=this.strVal;break;
            case BOOL: result.boolVal=this.boolVal;break;
            case NULL:
                break;
            case EMPTY:
                break;
            case MVEL:
                break;
            default:
                throw new RuntimeException("not managed type "+type);
        }
        return result;
    }
}
