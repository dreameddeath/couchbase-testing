package com.dreameddeath.rating.model.context;

import com.dreameddeath.common.annotation.DocumentProperty;
import com.dreameddeath.common.model.document.CouchbaseDocumentArrayList;
import com.dreameddeath.rating.model.cdr.CdrsBucketLink;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class StandardRatingContext extends AbstractRatingContext{
    @DocumentProperty("cdrsBucketLinks")
    private List<CdrsBucketLink> _cdrsBuckets=new ArrayList<CdrsBucketLink>();
    @DocumentProperty("guidingKeys")
    private List<RatingContextGuidingKey> _guidingKeys=new CouchbaseDocumentArrayList<RatingContextGuidingKey>(StandardRatingContext.this);
    @DocumentProperty("ratePlans")
    private List<RatingContextRatePlan> _ratePlans=new CouchbaseDocumentArrayList<RatingContextRatePlan>(StandardRatingContext.this);
    @DocumentProperty("sharedContexts")
    private List<RatingContextSharedLink> _sharedRatingCtxtLinks=new CouchbaseDocumentArrayList<RatingContextSharedLink>(StandardRatingContext.this);

    public List<CdrsBucketLink> getCdrsBuckets(){ return Collections.unmodifiableList(_cdrsBuckets); }
    public void setCdrsBucketLinks(List<CdrsBucketLink> cdrsBucketsLnk){ _cdrsBuckets.clear(); _cdrsBuckets.addAll(cdrsBucketsLnk); }
    public void addCdrsBucketLink(CdrsBucketLink cdrsBucketsLnk){  _cdrsBuckets.add(cdrsBucketsLnk); }

    public List<RatingContextGuidingKey> getGuidingKeys(){ return Collections.unmodifiableList(_guidingKeys); }
    public void setGuidingKeys(List<RatingContextGuidingKey> guidingKeys){ _guidingKeys.clear(); _guidingKeys.addAll(guidingKeys); }
    public void addGuidingKeys(List<RatingContextGuidingKey> guidingKeys){ _guidingKeys.addAll(guidingKeys); }
    public void addGuidingKey(RatingContextGuidingKey guidingKey){ _guidingKeys.add(guidingKey); }
    
    public List<RatingContextRatePlan> getRatePlans(){ return Collections.unmodifiableList(_ratePlans); }
    public void setRatePlans(List<RatingContextRatePlan> ratePlans){_ratePlans.clear();_ratePlans.addAll(ratePlans);}
    public void addRatePlans(List<RatingContextRatePlan> ratePlans){_ratePlans.addAll(ratePlans);}
    public void addRatePlan(RatingContextRatePlan ratePlan){ _ratePlans.add(ratePlan);}
    
    public List<RatingContextSharedLink> getSharedContexts(){return Collections.unmodifiableList(_sharedRatingCtxtLinks);}
    public void setSharedContexts(List<RatingContextSharedLink> sharedContexts){_sharedRatingCtxtLinks.clear();_sharedRatingCtxtLinks.addAll(sharedContexts);}
    public void addSharedContext(SharedRatingContext sharedContext){_sharedRatingCtxtLinks.add(sharedContext.newSharedContextLink());}
    
    
}