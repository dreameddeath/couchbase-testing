package com.dreameddeath.core.model.common;


import com.dreameddeath.core.model.property.HasParent;
import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

@JsonInclude(Include.NON_EMPTY)
@JsonIgnoreProperties(ignoreUnknown=true)
@JsonAutoDetect(getterVisibility=Visibility.NONE,fieldVisibility=Visibility.NONE,isGetterVisibility = Visibility.NONE,setterVisibility = Visibility.NONE,creatorVisibility = Visibility.NONE)
public abstract class RawCouchbaseDocumentElement implements HasParent {
    HasParent _parentElt;

    @Override
    public HasParent getParentElement() { return _parentElt;}
    @Override
    public void setParentElement(HasParent parentElt) {_parentElt=parentElt;}
}
