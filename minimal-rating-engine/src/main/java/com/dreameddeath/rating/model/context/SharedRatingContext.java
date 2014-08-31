package com.dreameddeath.rating.model.context;


public final class SharedRatingContext extends RatingContext {
    
    public RatingContextSharedLink newSharedContextLink(){
        return new RatingContextSharedLink(this);
    }

}