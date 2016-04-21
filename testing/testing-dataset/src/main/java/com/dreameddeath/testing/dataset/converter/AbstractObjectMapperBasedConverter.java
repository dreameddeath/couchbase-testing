package com.dreameddeath.testing.dataset.converter;

import com.dreameddeath.testing.dataset.runtime.model.DatasetResultValue;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Created by Christophe Jeunesse on 20/04/2016.
 */
public abstract class AbstractObjectMapperBasedConverter<T> implements IDatasetResultConverter<T> {
    private final JsonNodeConverter jsonNodeConverter=new JsonNodeConverter();
    private final ObjectMapper mapper;

    public AbstractObjectMapperBasedConverter(ObjectMapper mapper){
        this.mapper = mapper;
    }

    @Override
    public DatasetResultValue mapObject(T src) {
        return jsonNodeConverter.mapObject(mapper.valueToTree(src));
    }

    @Override
    public <TSUB extends T> TSUB mapResult(Class<TSUB> clazz, DatasetResultValue value) {
        JsonNode node = jsonNodeConverter.mapResult(JsonNode.class,value);
        try {
            return mapper.treeToValue(node, clazz);
        }
        catch(JsonProcessingException e){
            throw new RuntimeException(e);
        }
    }
}
