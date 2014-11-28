package com.dreameddeath.core.transcoder.json;

import com.dreameddeath.core.model.document.CouchbaseDocument;
import com.dreameddeath.core.model.document.CouchbaseDocumentElement;
import com.fasterxml.jackson.databind.BeanDescription;
import com.fasterxml.jackson.databind.DeserializationConfig;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.deser.BeanDeserializer;
import com.fasterxml.jackson.databind.deser.BeanDeserializerModifier;

/**
 * Created by ceaj8230 on 28/11/2014.
 */
public class CouchbaseBusinessDocumentDeserializerModifier extends BeanDeserializerModifier
{
    @Override
    public JsonDeserializer<?> modifyDeserializer(DeserializationConfig config,
                                                  BeanDescription beanDesc, JsonDeserializer<?> deserializer) {
        if ((CouchbaseDocument.class.isAssignableFrom(beanDesc.getBeanClass()) ||
                CouchbaseDocumentElement.class.isAssignableFrom(beanDesc.getBeanClass())) &&
                (deserializer instanceof  BeanDeserializer)
                )
        {
            return new CouchbaseBusinessDocumentDeserializer((BeanDeserializer)deserializer);
        }

        return super.modifyDeserializer(config, beanDesc, deserializer);
    }
}
