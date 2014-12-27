package com.dreameddeath.core.model.view;

import com.couchbase.client.java.document.json.JsonObject;
import com.dreameddeath.core.exception.view.ViewDecodingException;
import com.dreameddeath.core.exception.view.ViewEncodingException;

/**
 * Created by ceaj8230 on 22/12/2014.
 */
public interface IViewValueTranscoder<T> {
    public JsonObject encodeToJsonObject(T key) throws ViewEncodingException;
    public T decodefromJsonObject(JsonObject value) throws ViewDecodingException;
}
