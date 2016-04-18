package com.dreameddeath.testing.dataset.runtime.validator;

import com.dreameddeath.testing.dataset.model.*;
import com.dreameddeath.testing.dataset.runtime.MvelRuntimeContext;
import com.dreameddeath.testing.dataset.runtime.builder.DatasetBuilder;
import com.dreameddeath.testing.dataset.runtime.model.DatasetResultObject;
import com.dreameddeath.testing.dataset.runtime.model.DatasetResultValue;
import com.dreameddeath.testing.dataset.runtime.xpath.DatasetXPathProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.util.Iterator;
import java.util.List;

import static com.dreameddeath.testing.dataset.model.DatasetMeta.Type.*;

/**
 * Created by Christophe Jeunesse on 17/04/2016.
 */
public class DatasetValidator {
    private static final Logger LOG = LoggerFactory.getLogger(DatasetValidator.class);
    private final Dataset rootDataset;
    private final DatasetBuilder builder;
    private final DatasetXPathProcessor xPathProcessor;
    private final MvelRuntimeContext context;

    public DatasetValidator(Dataset dataset){
        this.rootDataset=dataset;
        this.builder=new DatasetBuilder(dataset);
        this.context = builder.getContext();
        this.xPathProcessor= builder.getXPathProcessor();
    }


    public boolean validate(DatasetResultValue value,String datasetElementName){
        for(DatasetElement element:rootDataset.getElements()){
            if(element.isValue() && element.getName().equalsIgnoreCase(datasetElementName)){
                return validate(value,element);
            }
            else{
                builder.run(element);
            }
        }
        throw new RuntimeException("The dataset element "+datasetElementName+" isn't found");
    }

    private boolean validate(DatasetResultValue value,DatasetElement refElement){
        DatasetValue refValue = new DatasetValue();
        switch (refElement.getType()){
            case ARRAY:
                refValue.setArrayValue(refElement.getArray());
                break;
            case OBJECT:
                refValue.setObjectValue(refElement.getObject());
                break;
            default:
                LOG.warn("Cannot manage element of type {}",refElement.getType());
                return false;
        }
        return validateValue(value,refValue,"");
    }

    public boolean validateValue(DatasetResultValue source, DatasetValue refValue,String currSourcepath){
        if(refValue.getType()==null){
            return true;
        }

        boolean result=true;
        result&=validateMetas(source, refValue.getMetas(),currSourcepath);
        if(refValue.getType()!= DatasetValue.Type.EMPTY){
            if(!refValue.isMvel() &&  source.getType()!=refValue.getType()){
                return false;
            }
            switch (source.getType()){
                case ARRAY:
                    //TODO manage ordering
                    Iterator<DatasetValue> refValIterator=refValue.getArrayVal().iterator();
                    int arrayPos=0;
                    for(DatasetResultValue subVal:source.getArrayVal().getValues()){
                        ++arrayPos;
                        if(refValIterator.hasNext()){
                            result&=validateValue(subVal,refValIterator.next(),currSourcepath+"["+arrayPos+"]");
                        }
                        else{
                            LOG.warn("The number of items in node {} is not the same <actual {},expected {}>",refValue.getFullPath(),source.getArrayVal().getValues().size(),refValue.getArrayVal().size());
                            result=false;
                        }
                    }
                    break;
                case OBJECT:
                    result &=validateNodes(source.getObject(),refValue.getObjVal().getNodes(),currSourcepath);
                    break;
                default:
                    Object value;
                    if(refValue.isMvel()||refValue.isTemplate()){
                        value=context.execute(refValue);
                        if(value!=null) {
                            if ((value instanceof Integer) || (value instanceof Short)) {
                                value = ((Number) value).longValue();
                            }
                            else if ((value instanceof Float) || (value instanceof Double)) {
                                value = new BigDecimal(((Number) value).doubleValue());
                            }
                        }
                    }
                    else{
                        value=refValue.getContent();
                    }
                    if(
                            ((source.getContent()==null) && (value!=null)) ||
                            (source.getContent()!=null) && !source.getContent().equals(source.getContent())
                       )
                    {
                        LOG.warn("Error in path {} : Content not matching <actual {},expected {}>",currSourcepath,source.getContent(),refValue.getContent());
                        result=false;
                    }
            }
        }

        return result;
    }

    public boolean validateNodes(DatasetResultObject object, List<DatasetObjectNode> nodes,String currSourcepath){
        boolean result=true;
        for(DatasetObjectNode node:nodes){
            DatasetXPathProcessor.XPathMatchingResult results = xPathProcessor.applyXPath(object,node.getXPath(),false);
            DatasetValue testingValue = node.getValue();

            result&= validateMetasOnGlobalList(results.getValues(),testingValue.getMetas(),currSourcepath);
            for(DatasetXPathProcessor.XPathMatchingResult.Entry entry:results){
                result&=validateValue(entry.getValue(),testingValue,currSourcepath+entry.getPath());
            }
        }
        return result;
    }



    public boolean validateMetasOnGlobalList(List<DatasetResultValue> values, List<DatasetMeta> metas,String currSourcepath){
        boolean result =true;
        for(DatasetMeta meta:metas){
            switch (meta.getType()){
                case NB_VALUES:
                    if(values.size()!=meta.getParam(0,Integer.class)){
                        //TODO manage messages/logs
                        LOG.warn("The number of items in node {} is not the same <actual {},expected {}>",currSourcepath,values.size(),meta.getParam(0,Integer.class));
                        result=false;
                    }
                    break;
                case NOT_EXISTING:
                    if(values.size()!=0){
                        //TODO manage messages/logs
                        LOG.warn("The number of items in node {} is not the same <actual {},expected 0>",currSourcepath,values.size());
                        result=false;
                    }
            }
        }

        return result;
    }

    public boolean validateMetas(DatasetResultValue value,List<DatasetMeta> metas,String currSourcepath){
        boolean result = true;
        for(DatasetMeta meta:metas){
            result&=validateMeta(value,meta,currSourcepath);
        }
        return result;
    }

    public boolean validateMeta(DatasetResultValue value, DatasetMeta meta,String currSourcepath){
        //boolean result=true;
        switch(meta.getType()){
            case NOT_NULL:
                if(!(value.isNew()||(value.getType()== DatasetValue.Type.NULL))){
                    LOG.warn("Error Rule [NOT_NULL] : Expecting not null value in path {}",currSourcepath);
                    return false;
                }
                break;
            case CONTAINS:
                if(value.getType()!= DatasetValue.Type.STRING){
                    LOG.warn("Error Rule [CONTAINS] : Expecting String content in path {} but found {} (val:<{}>)",currSourcepath,value.getType(),value.getContent());
                    return false;
                }
                else if(!value.getContent(String.class).contains(meta.getParam(0,String.class))) {
                    LOG.warn("Error Rule [CONTAINS] : In path {} string <{}> doesn't contains <{}>",currSourcepath,value.getContent(String.class),meta.getParam(0,String.class));
                    return false;
                }

                break;
            case MATCH:
                if(value.getType()!= DatasetValue.Type.STRING){
                    LOG.warn("Error Rule [MATCH] : Expecting String content in path {} but found {} (val:<{}>)",currSourcepath,value.getType(),value.getContent());
                    return false;
                }
                else if(!value.getContent(String.class).matches(meta.getParam(0,String.class))){
                    LOG.warn("Error Rule [MATCH] : In path {}, string <{}> doesn't match <{}>",currSourcepath,value.getContent(),meta.getParam(0,String.class));
                    return false;
                }
                //result&=(value.getType()== DatasetValue.Type.STRING) && value.getContent(String.class).matches(meta.getParam(0,String.class));
                break;
            case LT:
            case LTE:
            case GT:
            case GTE:
                if(!value.isComparable()){
                    LOG.warn("Error Rule [{}] : In path {} expecting content to be comparable but found type {}",meta.getType(),currSourcepath,value.getType());
                    return false;
                }
                else if(value.getType()!=meta.getParams().get(0).getType()){
                    LOG.warn("Error Rule [{}] : In path {} types not comparables (expected {}, found {})",meta.getType(),currSourcepath,meta.getParams().get(0).getType(),value.getType());
                    return false;
                }
                int compResult = value.getContent(Comparable.class).compareTo(meta.getParam(0,Comparable.class));
                boolean isNotValid = (meta.getType()==LT && compResult>=0)
                        || (meta.getType()==LTE && compResult>0)
                        || (meta.getType()==GT && compResult<=0)
                        || (meta.getType()==GTE && compResult<0);
                if(isNotValid){
                    LOG.warn("Error Rule [{}] : In path {} not valid value (expected {}, found {})",meta.getType(),currSourcepath,meta.getParam(0,Object.class),value.getContent());
                    return false;
                }

            case TYPE:
                if(!(value.getType().toString().equalsIgnoreCase(meta.getParam(0,String.class)))){
                    LOG.warn("Error Rule [{}] : In path {} not valid value (expected {}, found {})",meta.getType(),currSourcepath,meta.getParam(0,String.class).toUpperCase(),value.getType());
                    return false;
                }
            //case
        }

        return true;
    }

}
