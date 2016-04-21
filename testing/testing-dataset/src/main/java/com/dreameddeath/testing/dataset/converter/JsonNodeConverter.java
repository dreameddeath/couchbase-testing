package com.dreameddeath.testing.dataset.converter;

import com.dreameddeath.testing.dataset.runtime.model.DatasetResultArray;
import com.dreameddeath.testing.dataset.runtime.model.DatasetResultObject;
import com.dreameddeath.testing.dataset.runtime.model.DatasetResultValue;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.joda.JodaModule;
import org.joda.time.DateTime;

import java.math.BigDecimal;
import java.util.TimeZone;

/**
 * Created by Christophe Jeunesse on 20/04/2016.
 */
public class JsonNodeConverter implements IDatasetResultConverter<JsonNode> {
    public static final ObjectMapper OBJECT_MAPPER;
    static {
        OBJECT_MAPPER =new ObjectMapper();
        OBJECT_MAPPER.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
        OBJECT_MAPPER.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        OBJECT_MAPPER.configure(SerializationFeature.WRITE_EMPTY_JSON_ARRAYS, false);
        OBJECT_MAPPER.configure(SerializationFeature.WRITE_NULL_MAP_VALUES, false);
        OBJECT_MAPPER.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        //mapper.disable(MapperFeature.CAN_OVERRIDE_ACCESS_MODIFIERS);
        OBJECT_MAPPER.setTimeZone(TimeZone.getDefault());
        OBJECT_MAPPER.registerModule(new JodaModule());
    }

    public JsonNodeConverter(){

    }

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
    public <TSUB extends JsonNode> TSUB mapResult(Class<TSUB> clazz, DatasetResultValue value) {
        try {
            return (TSUB) OBJECT_MAPPER.readTree(OBJECT_MAPPER.writeValueAsString(value));
        }
        catch(Throwable e){
            throw new RuntimeException(e);
        }
    }
}
