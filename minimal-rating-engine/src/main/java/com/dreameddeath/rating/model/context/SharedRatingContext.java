package com.dreameddeath.rating.model.context;

import java.util.List;
import java.util.ArrayList;

import org.joda.time.DateTime;

import com.fasterxml.jackson.annotation.JsonProperty;

public final class SharedRatingContext extends AbstractRatingContext{
    
    public RatingContextSharedLink newSharedContextLink(){
        return new RatingContextSharedLink(this);
    }

}