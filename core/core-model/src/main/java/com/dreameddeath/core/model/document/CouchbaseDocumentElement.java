package com.dreameddeath.core.model.document;


import com.dreameddeath.core.model.property.HasParent;
import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonSetter;

import java.util.Collection;

@JsonInclude(Include.NON_EMPTY)
@JsonIgnoreProperties(ignoreUnknown=true)
@JsonAutoDetect(getterVisibility=Visibility.NONE,fieldVisibility=Visibility.NONE,isGetterVisibility = Visibility.NONE,setterVisibility = Visibility.NONE,creatorVisibility = Visibility.NONE)
public abstract class CouchbaseDocumentElement implements HasParent {
    HasParent _parentElt;


    @JsonSetter("@t")
    public void setClassTypeId(String type){

    }


    @Override
    public HasParent getParentElement() { return _parentElt;}
    @Override
    public void setParentElement(HasParent parentElt) {_parentElt=parentElt;}
}
