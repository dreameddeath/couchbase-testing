package com.dreameddeath.rating.model.context;

import com.dreameddeath.common.model.CouchbaseDocumentLink;
import com.dreameddeath.common.annotation.DocumentProperty;

public class RatingContextLink extends CouchbaseDocumentLink<AbstractRatingContext>{
    @DocumentProperty("@c")
    private String _class;
    
    public String getType() { return _class;}
    public void setType(String clazz) { _class=clazz;}
    
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