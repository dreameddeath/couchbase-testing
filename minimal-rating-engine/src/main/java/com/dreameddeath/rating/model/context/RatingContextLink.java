package com.dreameddeath.rating.model.context;

import com.dreameddeath.common.annotation.DocumentProperty;
import com.dreameddeath.common.model.document.CouchbaseDocumentLink;
import com.dreameddeath.common.model.property.StandardProperty;
import com.dreameddeath.common.model.property.Property;

public class RatingContextLink extends CouchbaseDocumentLink<AbstractRatingContext>{
    @DocumentProperty("@c")
    private Property<String> _class=new StandardProperty<String>(RatingContextLink.this);
    
    public String getType() { return _class.get();}
    public void setType(String clazz) { _class.set(clazz);}
    
    @Override
    public String toString(){
        String result ="{\n"+super.toString()+",\n";
        result+="type : "+getType();
        result+="}\n";
        return result;
    }
    
    public RatingContextLink(){}
    public RatingContextLink(AbstractRatingContext ratingCtxt){
        super(ratingCtxt);
        setType(ratingCtxt.getClass().getSimpleName());
    }
    
    public RatingContextLink(RatingContextLink srcLink){
        super(srcLink);
        setType(srcLink.getType());
    }
    
}