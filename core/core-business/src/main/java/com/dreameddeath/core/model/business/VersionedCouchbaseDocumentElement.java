package com.dreameddeath.core.model.business;

import com.dreameddeath.core.model.IVersionedDocument;
import com.dreameddeath.core.model.document.CouchbaseDocumentElement;
import com.dreameddeath.core.transcoder.json.CouchbaseDocumentTypeIdResolver;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.annotation.JsonTypeIdResolver;

/**
 * Created by ceaj8230 on 17/12/2014.
 */
@JsonTypeInfo(use= JsonTypeInfo.Id.CUSTOM, include= JsonTypeInfo.As.PROPERTY, property="@t",visible = true)
@JsonTypeIdResolver(CouchbaseDocumentTypeIdResolver.class)
public class VersionedCouchbaseDocumentElement extends CouchbaseDocumentElement implements IVersionedDocument {
    private String _documentFullVersionId;

    @JsonSetter("@t")
    public void setDocumentFullVersionId(String typeId){_documentFullVersionId = typeId;}
    public String getDocumentFullVersionId(){return _documentFullVersionId;}

}
