/*
 * Copyright Christophe Jeunesse
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */

package com.dreameddeath.core.transcoder.json;

import com.dreameddeath.core.model.annotation.DocumentEntity;
import com.dreameddeath.core.model.entity.model.EntityModelId;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.util.JsonParserSequence;
import com.fasterxml.jackson.databind.BeanProperty;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.jsontype.TypeDeserializer;
import com.fasterxml.jackson.databind.jsontype.TypeIdResolver;
import com.fasterxml.jackson.databind.jsontype.impl.AsPropertyTypeDeserializer;
import com.fasterxml.jackson.databind.util.TokenBuffer;

import java.io.IOException;
import java.lang.reflect.Modifier;

/**
 * Created by Christophe Jeunesse on 20/04/2016.
 */
public class CustomAsPropertyDeserializer extends AsPropertyTypeDeserializer {
    private final boolean isCouchbaseDocument;
    private final String defaultTypeId;
    private final String defaultDomainId;
    private final String defaultVersionId;
    //private final String nameOnlyId;
    //private final String partialId;

    public CustomAsPropertyDeserializer(JavaType bt, TypeIdResolver idRes, String typePropertyName, boolean typeIdVisible,JavaType defaultImpl, JsonTypeInfo.As inclusion) {
        super(bt, idRes, typePropertyName, typeIdVisible, defaultImpl, inclusion);
        EntityModelId modelId=null;
        if(bt.getRawClass().getAnnotation(DocumentEntity.class)!=null) {
            modelId = EntityModelId.build(bt.getRawClass().getAnnotation(DocumentEntity.class), bt.getRawClass());
        }

        if(Modifier.isAbstract(bt.getRawClass().getModifiers())||(modelId==null)) {
            defaultTypeId=null;
        }
        else{
            defaultTypeId=modelId.toString();
        }

        if(modelId!=null){
            defaultDomainId=modelId.getDomain();
            defaultVersionId=modelId.getEntityVersion().toString();
            //nameOnlyId=modelId.getName();
            //partialId=modelId.getName();
            isCouchbaseDocument=true;
        }
        else{
            defaultDomainId=null;
            defaultVersionId=null;
            //nameOnlyId=null;
            //partialId=null;
            isCouchbaseDocument=false;
        }
    }

    public CustomAsPropertyDeserializer(CustomAsPropertyDeserializer src, BeanProperty property) {
        super(src, property);
        this.isCouchbaseDocument=src.isCouchbaseDocument;
        this.defaultTypeId=src.defaultTypeId;
        this.defaultDomainId=src.defaultDomainId;
        this.defaultVersionId=src.defaultVersionId;
    }

    @Override
    public TypeDeserializer forProperty(BeanProperty prop) {
        return (prop == _property) ? this : new CustomAsPropertyDeserializer(this, prop);
    }


    @Override
    protected Object _deserializeTypedUsingDefaultImpl(JsonParser jp, DeserializationContext ctxt, TokenBuffer tb) throws IOException {
        if(defaultTypeId!=null){
            JsonDeserializer<Object> deser = _findDeserializer(ctxt, defaultTypeId);
            if (tb == null) {
                tb = new TokenBuffer(jp, ctxt);
            }
            if(_typeIdVisible) {
                tb.writeFieldName(_typePropertyName);
                tb.writeString(defaultTypeId);
            }
            tb.writeEndObject();
            JsonParser newJp=JsonParserSequence.createFlattened(false,tb.asParser(jp), jp);
            try{
                newJp.nextToken();
                return deser.deserialize(newJp, ctxt);
            }
            catch(IOException e){
                newJp.close();
                throw e;
            }
        }
        else{
            return super._deserializeTypedUsingDefaultImpl(jp,ctxt,tb);
        }
    }

    @Override
    protected Object _deserializeTypedForId(JsonParser jp, DeserializationContext ctxt, TokenBuffer tb) throws IOException{
        if(isCouchbaseDocument){
            String typeId = jp.getText();
            String effectiveId;
            int nbParts=1;
            int pos=typeId.indexOf(EntityModelId.ENTITY_SPERATOR);
            while(pos>0){
                ++nbParts;
                pos=typeId.indexOf(EntityModelId.ENTITY_SPERATOR,pos+1);
            }
            if(nbParts>=3){
                effectiveId=typeId;
            }
            else if(nbParts==1){
                effectiveId=defaultDomainId+EntityModelId.ENTITY_SPERATOR+typeId+EntityModelId.ENTITY_SPERATOR+defaultVersionId;
            }
            else{
                effectiveId=defaultDomainId+EntityModelId.ENTITY_SPERATOR+typeId;
            }
            JsonDeserializer<Object> deser = _findDeserializer(ctxt, effectiveId);
            if (_typeIdVisible) {
                if (tb == null) {
                    tb = new TokenBuffer(jp, ctxt);
                }
                tb.writeFieldName(jp.getCurrentName());
                tb.writeString(typeId);
            }
            if (tb != null) { // need to put back skipped properties?
                jp = JsonParserSequence.createFlattened(false,tb.asParser(jp), jp);
            }
            // Must point to the next value; tb had no current, jp pointed to VALUE_STRING:
            jp.nextToken(); // to skip past String value
            // deserializer should take care of closing END_OBJECT as well
            return deser.deserialize(jp, ctxt);
        }
        else{
            return super._deserializeTypedForId(jp,ctxt,tb);
        }
    }
}
