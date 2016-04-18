package com.dreameddeath.testing.dataset.runtime.xpath;

import com.dreameddeath.testing.dataset.model.DatasetRange;
import com.dreameddeath.testing.dataset.model.DatasetValue;
import com.dreameddeath.testing.dataset.model.DatasetXPath;
import com.dreameddeath.testing.dataset.model.DatasetXPathPart;
import com.dreameddeath.testing.dataset.runtime.model.DatasetResultArray;
import com.dreameddeath.testing.dataset.runtime.model.DatasetResultObject;
import com.dreameddeath.testing.dataset.runtime.model.DatasetResultValue;

import java.util.*;

/**
 * Created by Christophe Jeunesse on 18/04/2016.
 */
public class DatasetXPathProcessor {


    public boolean hasNext(DatasetXPath xpath,int pos){
        return pos<xpath.getParts().size();
    }

    public boolean needRecursion(DatasetXPath xpath,int pos){
        return (pos+1)<xpath.getParts().size();
    }


    public XPathMatchingResult applyXPath(DatasetResultObject object, DatasetXPath xpath,boolean createMissingParts){
        return applyXPath(object,xpath,0,createMissingParts,false);
    }


    public XPathMatchingResult applyXPath(DatasetResultArray array, DatasetXPath xpath,boolean createMissingParts){
        return applyXPath(array,xpath,0,createMissingParts,false);
    }

    public XPathMatchingResult applyXPath(DatasetResultValue value, DatasetXPath xpath,boolean createMissingParts){
        return applyXPath(value,xpath,0,createMissingParts,false);
    }



    private XPathMatchingResult applyXPath(DatasetResultObject object, DatasetXPath xpath, int xPathPartPos, boolean createMissingParts,boolean isInMatchCase){
        XPathMatchingResult resultValues=new XPathMatchingResult(xPathPartPos!=0);
        if(hasNext(xpath,xPathPartPos)){
            DatasetXPathPart xPathPart = xpath.getParts().get(xPathPartPos);
            switch (xPathPart.getType()){
                case FIELD_NAME:
                    DatasetResultValue value= createMissingParts?object.getOrCreate(xPathPart.getLocalName()):object.get(xPathPart.getLocalName());
                    resultValues.addResults(xPathPart.getLocalName(),applyXPath(value,xpath,xPathPartPos+1,createMissingParts,isInMatchCase));
                    break;
                case MATCH_ALL:
                    for(Map.Entry<String,DatasetResultValue> entry:object.getValuesMap().entrySet()){
                        resultValues.addResults(entry.getKey(),applyXPath(entry.getValue(),xpath,xPathPartPos+1,createMissingParts,true));
                    }
                    break;
                case MATCH_ALL_RECURSIVE:
                    for(Map.Entry<String,DatasetResultValue> entry:object.getValuesMap().entrySet()){
                        resultValues.addResults(entry.getKey(),applyXPath(entry.getValue(),xpath,xPathPartPos+1,createMissingParts,true));
                        if(entry.getValue().getType()== DatasetValue.Type.OBJECT || entry.getValue().getType()== DatasetValue.Type.ARRAY) {
                            resultValues.addResults(entry.getKey(),applyXPath(entry.getValue(), xpath, xPathPartPos, createMissingParts,true));
                        }
                    }
                    break;
                case PREDICATE:
                    //TODO
                default:
                    if(!isInMatchCase) {
                        throw new RuntimeException("Type <" + xPathPart.getType() + "> Not managed yet");
                    }
            }
        }
        return resultValues;
    }

    private XPathMatchingResult applyXPath(DatasetResultValue value, DatasetXPath xpath, int xPathPartPos, boolean createMissingParts,boolean isInMatchCase){
        XPathMatchingResult resultValues=new XPathMatchingResult(xPathPartPos!=0);
        if(!hasNext(xpath,xPathPartPos)){
            resultValues.addResult(value);
        }
        else {
            if (createMissingParts && value.isNew()) {
                if (xpath.getParts().get(xPathPartPos).getType() == DatasetXPathPart.PartType.ARRAY_RANGE) {
                    value.setArray(new DatasetResultArray());
                } else {
                    value.setObject(new DatasetResultObject());
                }
            }
            switch (value.getType()) {
                case OBJECT:
                    resultValues.addResults(applyXPath(value.getObject(), xpath, xPathPartPos, createMissingParts,isInMatchCase));
                    break;
                case ARRAY:
                    resultValues.addResults(applyXPath(value.getArrayVal(), xpath, xPathPartPos, createMissingParts,isInMatchCase));
                    break;
                default:
                    if(!isInMatchCase) {
                        throw new RuntimeException("Not managed Type " + value.getType());
                    }
            }
        }
        return resultValues;
    }



    private XPathMatchingResult applyXPath(DatasetResultArray array, DatasetXPath xpath, int xPathPartPos, boolean createMissingParts,boolean isInMatchCase){
        XPathMatchingResult resultValues=new XPathMatchingResult(xPathPartPos!=0);
        if(hasNext(xpath,xPathPartPos)){
            DatasetXPathPart xPathPart = xpath.getParts().get(xPathPartPos);
            DatasetRange range = xPathPart.getRange();
            switch (xPathPart.getType()){
                case ARRAY_RANGE:
                    if(range.isExact()){
                        DatasetResultValue value=(createMissingParts && !isInMatchCase)?array.getOrCreate(range.getExact()):array.get(range.getExact());
                        resultValues.addResults("["+range.getExact()+"]",applyXPath(value,xpath,xPathPartPos+1,createMissingParts,isInMatchCase));
                    }
                    else{
                        List<DatasetResultValue> values=(createMissingParts && !isInMatchCase)?array.getOrCreate(range.getMin(),range.getMax()):array.get(range.getMin(),range.getMax());
                        int arrayPos=range.getMin()-1;
                        for(DatasetResultValue value:values) {
                            ++arrayPos;
                            resultValues.addResults("["+arrayPos+"]",applyXPath(value, xpath, xPathPartPos + 1, createMissingParts,isInMatchCase));
                        }
                    }
                    break;
                default:
                    if(!isInMatchCase) {
                        throw new RuntimeException("Type <" + xPathPart.getType() + "> Not managed yet");
                    }
            }
        }
        return resultValues;
    }


    public static class XPathMatchingResult implements Iterable<XPathMatchingResult.Entry>{
        private final boolean isSubPath;
        private List<String> paths=new ArrayList<>();
        private List<DatasetResultValue> resultValues=new ArrayList<>();

        public XPathMatchingResult(boolean isSubPath){
            this.isSubPath=isSubPath;
        }

        public void addResult(DatasetResultValue value){
            paths.add("");
            resultValues.add(value);
        }

        public void addResult(String prefix,String path,DatasetResultValue value){
            StringBuilder sb = new StringBuilder();
            sb.append(prefix);
            if(isSubPath && !path.startsWith("[")){
                sb.append(".");
            }
            sb.append(path);
            paths.add(sb.toString());
            resultValues.add(value);
        }

        public void addResult(String prefix,Entry result){
            addResult(prefix,result.getPath(),result.getValue());
        }

        public void addResults(String prefix,XPathMatchingResult results){
            for(Entry result:results){
                addResult(prefix,result);
            }
        }

        public void addResults(XPathMatchingResult results){
            for(Entry result:results){
                addResult(result);
            }
        }

        private void addResult(Entry result){
            paths.add(result.path);
            resultValues.add(result.getValue());
        }

        public List<DatasetResultValue> getValues(){
            return Collections.unmodifiableList(resultValues);
        }


        public class Entry{
            private final String path;
            private final DatasetResultValue value;
            public Entry(String path,DatasetResultValue value){
                this.path= path;
                this.value=value;
            }

            public DatasetResultValue getValue() {
                return value;
            }

            public String getPath() {
                return path;
            }
        }

        @Override
        public Iterator<Entry> iterator() {
            return new ResultIterator();
        }

        private class ResultIterator implements Iterator<Entry>{
            private final Iterator<String> pathIterator;
            private final Iterator<DatasetResultValue> resultValueIterator;

            public ResultIterator(){
                pathIterator=paths.iterator();
                resultValueIterator=resultValues.iterator();
            }


            @Override
            public boolean hasNext() {
                return pathIterator.hasNext() && resultValueIterator.hasNext();
            }

            @Override
            public Entry next() {
                return new Entry(pathIterator.next(),resultValueIterator.next());
            }
        }
    }
}
