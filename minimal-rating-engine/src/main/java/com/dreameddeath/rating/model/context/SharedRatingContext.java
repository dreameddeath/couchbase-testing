package com.dreameddeath.rating.model.context;


public final class SharedRatingContext extends AbstractRatingContext{
    
    public RatingContextSharedLink newSharedContextLink(){
        return new RatingContextSharedLink(this);
    }

}