package com.dreameddeath.rating.storage;

import com.dreameddeath.common.storage.CouchbaseConstants;
import com.dreameddeath.common.storage.GenericJacksonTranscoder;

import com.dreameddeath.rating.model.context.SharedRatingContext;

/**
*  Class used to perform storage 
*/
public class SharedRatingContextTranscoder extends GenericJacksonTranscoder<SharedRatingContext>{
    public SharedRatingContextTranscoder(){
        super(this.class);
    }
}
