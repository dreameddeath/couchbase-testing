package com.dreameddeath.rating.storage;

import com.dreameddeath.common.storage.CouchbaseConstants;
import com.dreameddeath.common.storage.GenericJacksonTranscoder;

import com.dreameddeath.rating.model.context.StandardRatingContext;

/**
*  Class used to perform storage 
*/
public class StandardRatingContextTranscoder extends GenericJacksonTranscoder<StandardRatingContext>{
    public StandardRatingContextTranscoder(){
        super(this.class);
    }
}
