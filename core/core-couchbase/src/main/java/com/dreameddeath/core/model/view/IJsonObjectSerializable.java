package com.dreameddeath.core.model.view;

import com.couchbase.client.java.document.json.JsonObject;

/**
 * Created by ceaj8230 on 18/12/2014.
 */
public interface IJsonObjectSerializable {
    public JsonObject toJsonObject();
}
