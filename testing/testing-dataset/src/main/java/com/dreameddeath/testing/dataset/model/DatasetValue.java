package com.dreameddeath.testing.dataset.model;

import com.dreameddeath.testing.dataset.utils.DatasetUtils;
import org.joda.time.DateTime;
import org.mvel2.templates.CompiledTemplate;
import org.mvel2.templates.TemplateCompiler;
import org.mvel2.templates.res.EndNode;
import org.mvel2.templates.res.Node;
import org.mvel2.templates.res.TextNode;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


/**
 * Created by Christophe Jeunesse on 13/04/2016.
 */
public class DatasetValue {
    private String path=null;
    private Dataset parent=null;
    private DatasetElement parentElement=null;
    private Type type;

    private List<DatasetMeta> metaList=new ArrayList<>();

    private String strValue;
    private BigDecimal decimalVal;
    private Long longVal;
    private DatasetObject objVal;
    private List<DatasetValue> arrayVal = new ArrayList<>();
    private Boolean boolVal;
    private DateTime dateTimeVal;
    private boolean isTemplate=false;
    private CompiledTemplate template=null;
    private DatasetMvel mvel=null;
    
    public void setStrValue(String value){
        type = Type.STRING;
        strValue = DatasetUtils.parseJavaEncodedString(value);
        isTemplate=false;
        try {
            CompiledTemplate compiledExpression = TemplateCompiler.compileTemplate(strValue);
            Node node=compiledExpression.getRoot();
            do{
                if(!(node instanceof TextNode) && !(node instanceof EndNode)){
                    isTemplate=true;
                    break;
                }
                node = node.getNext();
            }
            while(node!=null);
        }
        catch(Throwable e){
            isTemplate=false;
        }
    }

    public void addMeta(DatasetMeta meta){
        this.metaList.add(meta);
    }

    
    public void setMvel(DatasetMvel mvel){
        this.mvel = mvel;
        this.type=Type.MVEL;
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

    public void setDateTime(DateTime dateTime) {
        this.dateTimeVal = dateTime;
        this.type = DatasetValue.Type.DATETIME;
    }

    public void setDateTime(String dateTimeValStr) {
        setDateTime(DateTime.parse(dateTimeValStr));
    }
    public void setEmpty(){
        this.type = Type.EMPTY;
    }

    public void setNull(){
        this.type = Type.NULL;
    }

    public Type getType() {
        return type;
    }

    public List<DatasetMeta> getMetas() {
        return metaList;
    }

    public Object getContent(){
        switch (type){
            case STRING:return strValue;
            case DECIMAL:return decimalVal;
            case LONG:return longVal;
            case OBJECT:return objVal;
            case ARRAY:return Collections.unmodifiableList(arrayVal);
            case BOOL:return boolVal;
            case DATETIME:return dateTimeVal;
            case NULL:return null;
            default:return null;
        }
    }

    public <T> T getContent(Class<T> clazz){
        Object content=this.getContent();
        if(content==null){
            return null;
        }
        else if(clazz.isAssignableFrom(content.getClass())){
            return (T)this.getContent();
        }
        else{
            throw new RuntimeException("Cannot get class "+clazz.getName()+" from type "+type+" on content <"+content.getClass().getName()+">");
        }
    }

    public String getStrValue() {
        if(isTemplate){
            throw new RuntimeException("the value of path "+path+" is a template");
        }
        return strValue;
    }
    
    public boolean isMvel(){
        return type==Type.MVEL; 
    }

    public boolean isTemplate(){
        return (type==Type.STRING) && isTemplate;
    }

    public CompiledTemplate getTemplate(){
        return template;
    }

    public BigDecimal getDecimalVal() {
        return decimalVal;
    }

    public Long getLongVal() {
        return longVal;
    }

    public DatasetObject getObjVal() {
        return objVal;
    }

    public List<DatasetValue> getArrayVal() {
        return Collections.unmodifiableList(arrayVal);
    }

    public Boolean getBoolVal() {
        return boolVal;
    }

    public DatasetMvel getMvel() {
        return mvel;
    }

    public DateTime getDateTimeVal() {
        return dateTimeVal;
    }

    public enum Type{
        STRING,
        DECIMAL,
        LONG,
        OBJECT,
        ARRAY,
        DATETIME,
        BOOL,
        NULL,
        EMPTY,
        MVEL
    }

    public void prepare(Dataset parent,DatasetElement parentElt,String path){
        this.parent = parent;
        this.parentElement = parentElt;
        this.path=path;
        metaList.forEach(datasetMeta -> datasetMeta.prepare(parent,parentElt,path));

        switch (type) {
            case OBJECT:
                objVal.prepare(parent, parentElt, path);
                break;
            case ARRAY:
                Integer pos = 0;
                for (DatasetValue val : this.arrayVal) {
                    val.prepare(parent, parentElt, path+"[" + (pos++) + "]");
                }
                break;
            case STRING:
                if(isTemplate){
                    template=TemplateCompiler.compileTemplate(strValue,parent.getParserContext());
                }
                break;
            case MVEL:
                mvel.prepare(parent,parentElt);
            default:
                //Nothing to do
        }
    }

    public String getFullPath(){
        return this.path;
    }
}
