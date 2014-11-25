package com.dreameddeath.core.transcoder.json;


import com.dreameddeath.core.exception.transcoder.DocumentDecodingException;
import com.dreameddeath.core.exception.transcoder.DocumentEncodingException;
import com.dreameddeath.core.model.document.CouchbaseDocument;
import com.dreameddeath.core.transcoder.ITranscoder;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.joda.JodaModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;


public class GenericJacksonTranscoder<T extends CouchbaseDocument> implements ITranscoder<T> {
    private final static Logger logger = LoggerFactory.getLogger(GenericJacksonTranscoder.class);

    private static final ObjectMapper _mapper;
    static {
        _mapper = new ObjectMapper();
        _mapper.disable(MapperFeature.CAN_OVERRIDE_ACCESS_MODIFIERS);
        _mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        _mapper.setAnnotationIntrospector(new CouchbaseDocumentIntrospector());
        _mapper.registerModule(new JodaModule());
    }


    private final Class<T> _dummyClass;


    public GenericJacksonTranscoder(Class<T> clazz){
        _dummyClass = clazz;
        try {
            //_mapper.getSerializerProvider().findTypedValueSerializer(clazz, true, null);
        }
        catch (Exception e){
            logger.error("Error during transcoder init for class <{}>",clazz.getName(),e);
            throw new RuntimeException("Error during transcoder init for class <"+clazz.getName()+">");
        }
    }

    @Override
    public Class<T> getBaseClass() {return _dummyClass;}

    @Override
    public T decode(byte[] content) throws DocumentDecodingException{
        try {
            T result = _mapper.readValue(content,getBaseClass());
            result.getBaseMeta().setDbSize(content.length);
            return result;
        }
        catch (JsonParseException e){
            throw new DocumentDecodingException("Error during decoding of data using GenericJacksonCouchbaseTranscoder<"+getBaseClass().getName()+"> :",content,e);
        }
        catch (JsonMappingException e) {
            throw new DocumentDecodingException("Error during decoding of data using GenericJacksonCouchbaseTranscoder<"+getBaseClass().getName()+"> :",content,e);
        }
        catch(IOException e){
            throw new DocumentDecodingException("Error during decoding of data using GenericJacksonCouchbaseTranscoder<"+getBaseClass().getName()+"> :",content,e);
        }
    }



    @Override
    public byte[] encode(T doc) throws DocumentEncodingException{
        try {
            return _mapper.writeValueAsBytes(doc);
        }
        catch (JsonProcessingException e){
            throw new DocumentEncodingException(doc,"Error during encoding of data using GenericJacksonCouchbaseTranscoder<"+getBaseClass().getName()+">",e);
        }
    }
}
