package com.dreameddeath.testing.dataset.runtime;

import com.dreameddeath.testing.dataset.model.*;
import com.google.common.base.Preconditions;

import java.util.ArrayList;
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

    public DatasetBuilder(Dataset dataset){
        this.rootDataset=dataset;
        context = new MvelRuntimeContext(dataset);
    }

    public DatasetResultValue getDatasetResultByName(String name){
        return resultingDatasetMap.get(name);
    }

    public void run(){
        run(rootDataset);
    }

    protected void run(Dataset dataset){
        String currDatasetElementName=null;
        for(DatasetElement element:dataset.getElements()){
            DatasetResultValue resultValue=null;
            switch (element.getType()){
                case OBJECT:
                    resultValue = new DatasetResultValue();
                    resultValue.setObject(buildObject(element.getObject()));
                    break;
                case ARRAY:
                    resultValue = new DatasetResultValue();
                    resultValue.setArray(buildArray(element.getArray()));
                    break;
                case MVEL:
                    context.execute(element.getMvel());break;
                case DIRECTIVE:
                    switch (element.getDirective().getType()){
                        case DATASET_ELT_NAME:
                            currDatasetElementName = element.getDirective().getParams().get(0);
                            break;
                        case IMPORT:
                            run(element.getDirective().getImportedDataset());
                            break;
                    }
            }
            if(resultValue!=null){
                if(currDatasetElementName==null){
                    currDatasetElementName = "anonymous_dataset_#"+resultingDatasetMap.values().size();
                }
                resultingDatasetMap.putIfAbsent(currDatasetElementName,resultValue);
                currDatasetElementName=null;
            }
        }
    }

    public DatasetResultObject buildObject(DatasetObject objectDef){
        DatasetResultObject result = new DatasetResultObject();
        for(DatasetObjectNode node:objectDef.getNodes()){
            List<DatasetResultValue> resultValues = createOrFindFromXPath(result,node.getXPath());
            for(DatasetResultValue value:resultValues){
                value.setValue(buildValue(node.getValue()));
            }
        }
        return result;
    }

    public DatasetResultArray buildArray(List<DatasetValue> valuesDef){
        DatasetResultArray result=new DatasetResultArray();
        for(DatasetValue currValueDef:valuesDef){
            result.add(buildValue(currValueDef));
        }
        return result;
    }

    public DatasetResultValue buildValue(DatasetValue valueDef){
        DatasetResultValue newValue=new DatasetResultValue();
        switch(valueDef.getType()){
            case ARRAY:
                newValue.setArray(buildArray(valueDef.getArrayVal()));
                break;
            case OBJECT:
                newValue.setObject(buildObject(valueDef.getObjVal()));
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
                newValue=null;
        }
        return newValue;
    }


    public List<DatasetResultValue> createOrFindFromXPath(DatasetResultObject obj, DatasetXPath xpath){
        List<DatasetResultValue> valueResults=new ArrayList<>();
        DatasetResultValue initResultVal=new DatasetResultValue();
        initResultVal.setObject(obj);
        valueResults.add(initResultVal);
        for(int posSubPath=0;posSubPath<xpath.getParts().size();++posSubPath){
            valueResults = createOrFindFromXPathSubPath(valueResults,xpath,posSubPath);
        }
        return valueResults;
    }

    public List<DatasetResultValue> createOrFindFromXPathSubPath(List<DatasetResultValue> values,DatasetXPath xpath,int subPathPos){
        List<DatasetResultValue> valueResults=new ArrayList<>();
        DatasetXPathPart subPathPart=xpath.getParts().get(subPathPos);
        for(DatasetResultValue srcVal:values) {
            Preconditions.checkArgument(srcVal.getType()== DatasetValue.Type.OBJECT);
            List<DatasetResultValue> newIntermediateResult=new ArrayList<>();
            DatasetResultObject currSrcObj=srcVal.getObject();
            switch (subPathPart.getType()) {
                case STD:
                    newIntermediateResult.add(currSrcObj.getOrCreate(subPathPart.getLocalName()));
                    break;
                case MATCH_ALL:
                    newIntermediateResult.addAll(currSrcObj.getAll());
                    break;
                default:
                    throw new RuntimeException(subPathPart.getType()+" isn't managed yet");
            }


            DatasetRange range = subPathPart.getRange();
            for(DatasetResultValue intermediateValue:newIntermediateResult){
                if(range!=null){
                    if(intermediateValue.isNew()){
                        intermediateValue.setArray(new DatasetResultArray());
                    }
                    Preconditions.checkArgument(intermediateValue.getType()== DatasetValue.Type.ARRAY);
                    DatasetResultArray intermediateValueArray=intermediateValue.getArrayVal();
                    if(range.isExact()){
                        valueResults.add(intermediateValueArray.getOrCreate(range.getExact()));
                    }
                    else{
                        valueResults.addAll(intermediateValueArray.getOrCreate(range.getMin(),range.getMax()));
                    }
                }
                else{
                    valueResults.add(intermediateValue);
                }
            }
        }

        for(DatasetResultValue resultValue:valueResults){
            if(resultValue.isNew()){
                resultValue.setObject(new DatasetResultObject());
            }
        }
        return valueResults;
    }

}
