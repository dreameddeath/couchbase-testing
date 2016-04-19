package com.dreameddeath.testing.dataset.runtime.builder;

import com.dreameddeath.testing.dataset.model.*;
import com.dreameddeath.testing.dataset.runtime.MvelRuntimeContext;
import com.dreameddeath.testing.dataset.runtime.model.DatasetResultArray;
import com.dreameddeath.testing.dataset.runtime.model.DatasetResultObject;
import com.dreameddeath.testing.dataset.runtime.model.DatasetResultValue;
import com.dreameddeath.testing.dataset.runtime.xpath.DatasetXPathProcessor;
import org.joda.time.DateTime;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Christophe Jeunesse on 17/04/2016.
 */
public class DatasetBuilder {
    private final Map<String,DatasetResultValue> resultingDatasetMap = new HashMap<>();
    private final MvelRuntimeContext context;
    private final Dataset rootDataset;
    private final DatasetXPathProcessor xPathProcessor;

    public DatasetBuilder(Dataset dataset){
        this(dataset, Collections.emptyMap());
    }
    public DatasetBuilder(Dataset dataset,Map<String,Object>params){
        this.rootDataset=dataset;
        context = new MvelRuntimeContext(dataset,params);
        xPathProcessor = new DatasetXPathProcessor();
    }

    public DatasetXPathProcessor getXPathProcessor(){
        return xPathProcessor;
    }
    public MvelRuntimeContext getContext(){
        return context;
    }

    public DatasetResultValue getDatasetResultByName(String name){
        return resultingDatasetMap.get(name);
    }

    public DatasetResultValue build(String datasetElementName) {
        for(DatasetElement element: rootDataset.getElements()){
            run(element);
            DatasetResultValue resultValue=resultingDatasetMap.get(datasetElementName);
            if(resultValue!=null){
                return resultValue;
            }
        }
        throw new RuntimeException("Cannot find dataset element "+datasetElementName +" in dataset "+rootDataset.getName());
    }

    public void run(){
        run(rootDataset);
    }

    protected void run(Dataset dataset){
        String currDatasetElementName=null;
        for(DatasetElement element:dataset.getElements()){
            run(element);
        }
    }

    public DatasetResultValue run(DatasetElement element){
        DatasetResultValue resultValue=null;
        switch (element.getType()){
            case OBJECT:
                resultValue = new DatasetResultValue();
                resultValue.setObject(buildObject(initObject(element.getMetaList()),element.getObject()));
                break;
            case ARRAY:
                resultValue = new DatasetResultValue();
                resultValue.setArray(buildArray(initArray(element.getMetaList()),element.getArray()));
                break;
            case MVEL:
                context.execute(element.getMvel());break;
            case DIRECTIVE:
                switch (element.getDirective().getType()){
                    case IMPORT:
                        run(element.getDirective().getImportedDataset());
                        break;
                }
        }
        if(resultValue!=null){
            String currDatasetElementName= element.getName();
            if(currDatasetElementName==null){
                currDatasetElementName = "anonymous_dataset_#"+resultingDatasetMap.values().size();
            }

            resultingDatasetMap.putIfAbsent(currDatasetElementName,resultValue);
        }
        return resultValue;
    }

    protected DatasetResultObject initObject(List<DatasetMeta> metas){
        for(DatasetMeta meta:metas){
            if(meta.getType()== DatasetMeta.Type.INIT_FROM){
                String name=meta.getParam(0,String.class);
                DatasetResultValue initValue=resultingDatasetMap.get(name);
                if(initValue!=null){
                    if(initValue.getType()== DatasetValue.Type.OBJECT){
                        return initValue.getObject().clone();
                    }
                    else{
                        throw new RuntimeException("The dataset "+name+ " isn't an object but "+initValue.getType());
                    }
                }
                throw new RuntimeException("Cannot find dataset <"+name+">");
            }
        }
        return new DatasetResultObject();
    }


    protected DatasetResultArray initArray(List<DatasetMeta> metas){
        for(DatasetMeta meta:metas){
            if(meta.getType()== DatasetMeta.Type.INIT_FROM){
                String name=meta.getParam(0,String.class);
                DatasetResultValue initValue=resultingDatasetMap.get(name);
                if(initValue!=null){
                    if(initValue.getType()== DatasetValue.Type.ARRAY){
                        return initValue.getArrayVal().clone();
                    }
                    else{
                        throw new RuntimeException("The dataset "+name+ " isn't an array but "+initValue.getType());
                    }
                }
                throw new RuntimeException("Cannot find dataset <"+name+">");
            }
        }
        return new DatasetResultArray();
    }


    protected DatasetResultObject buildObject(DatasetResultObject result,DatasetObject objectDef){
        for(DatasetObjectNode node:objectDef.getNodes()){
            List<DatasetResultValue> resultValues = xPathProcessor.applyXPath(result,node.getXPath(),true).getValues();
            for(DatasetResultValue value:resultValues){
                value.setValue(buildValue(node.getValue()));
            }
        }
        return result;
    }

    protected DatasetResultArray buildArray(DatasetResultArray result,List<DatasetValue> valuesDef){
        //DatasetResultArray result=new DatasetResultArray();

        for(DatasetValue currValueDef:valuesDef){
            result.add(buildValue(currValueDef));
        }
        return result;
    }

    protected DatasetResultValue buildValue(DatasetValue valueDef){
        DatasetResultValue newValue=new DatasetResultValue();

        if(valueDef.isTemplate()||valueDef.isMvel()){
            Object result=context.execute(valueDef);
            if(result instanceof Boolean){
                newValue.setBool((Boolean)result);
            }
            else if(result instanceof DateTime){
                newValue.setDateTime((DateTime) result);
            }
            else if(result instanceof String){
                newValue.setStr((String)result);
            }
            else if((result instanceof Integer)||(result instanceof Long)||(result instanceof Short)){
                newValue.setLong(((Number)result).longValue());
            }
            else if(result instanceof BigDecimal){
                newValue.setDecimal((BigDecimal)result);
            }
            else if((result instanceof Float)||(result instanceof Double)){
                newValue.setDecimal(new BigDecimal(((Number)result).doubleValue()));
            }
            else {
                throw new RuntimeException("Not managed class type "+result.getClass().getName()+" for node <"+valueDef.getFullPath()+">");
            }
        }
        else {
            switch (valueDef.getType()) {
                case ARRAY:
                    newValue.setArray(buildArray(initArray(valueDef.getMetas()),valueDef.getArrayVal()));
                    break;
                case OBJECT:
                    newValue.setObject(buildObject(initObject(valueDef.getMetas()),valueDef.getObjVal()));
                    break;
                case DECIMAL:
                    newValue.setDecimal(valueDef.getDecimalVal());
                    break;
                case STRING:
                    newValue.setStr(valueDef.getStrValue());
                    break;
                case LONG:
                    newValue.setLong(valueDef.getLongVal());
                    break;
                case BOOL:
                    newValue.setBool(valueDef.getBoolVal());
                default:
                    newValue = null;
            }
        }
        return newValue;
    }

}
