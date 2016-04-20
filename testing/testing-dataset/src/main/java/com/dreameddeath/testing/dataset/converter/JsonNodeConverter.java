package com.dreameddeath.testing.dataset.converter;

import com.dreameddeath.testing.dataset.runtime.model.DatasetResultArray;
import com.dreameddeath.testing.dataset.runtime.model.DatasetResultObject;
import com.dreameddeath.testing.dataset.runtime.model.DatasetResultValue;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.joda.time.DateTime;

import java.math.BigDecimal;

/**
 * Created by Christophe Jeunesse on 20/04/2016.
 */
public class JsonNodeConverter implements IDatasetResultConverter<JsonNode> {
    private ObjectMapper mapper=new ObjectMapper();

    @Override
    public boolean canMap(Class<?> clazz) {
        return JsonNode.class.isAssignableFrom(clazz);
    }

    @Override
    public DatasetResultValue mapObject(JsonNode src) {
        return mapJsonNodeValue(src);
    }

    private DatasetResultValue mapJsonNodeValue(JsonNode src) {
        DatasetResultValue result = new DatasetResultValue();
        if(src.isNull()){
            return null;
        }
        else if(src.isArray()){
            result.setArray(mapJsonNodeAsArray(src));
        }
        else if(src.isObject()){
            result.setObject(mapJsonAsObject(src));
        }
        else if(src.isBoolean()){
            result.setBool(src.asBoolean());
        }
        else if(src.isFloatingPointNumber()){
            result.setDecimal(new BigDecimal(src.asText()));
        }
        else if(src.isIntegralNumber()){
            result.setLong(src.longValue());
        }
        else {
            try {
                result.setDateTime(DateTime.parse(src.asText()));
            }
            catch(Throwable e){
                result.setStr(src.asText());
            }
        }
        return result;
    }

    private DatasetResultObject mapJsonAsObject(JsonNode src) {
        DatasetResultObject newObject=new DatasetResultObject();
        src.fields().forEachRemaining(entry->newObject.getOrCreate(entry.getKey()).setValue(mapJsonNodeValue(entry.getValue())));
        return newObject;
    }

    private DatasetResultArray mapJsonNodeAsArray(JsonNode src) {
        DatasetResultArray newArray=new DatasetResultArray();
        src.forEach(subNode->newArray.add(mapJsonNodeValue(subNode)));
        return newArray;
    }

    @Override
    public JsonNode mapResult(DatasetResultValue value) {
        try {
            return mapper.readTree(mapper.writeValueAsString(value));
        }
        catch(Throwable e){
            throw new RuntimeException(e);
        }
    }
}
