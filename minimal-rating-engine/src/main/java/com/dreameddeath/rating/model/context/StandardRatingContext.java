/*
 * Copyright Christophe Jeunesse
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.dreameddeath.rating.model.context;

import com.dreameddeath.core.annotation.DocumentProperty;
import com.dreameddeath.core.model.property.ListProperty;
import com.dreameddeath.core.model.property.impl.ArrayListProperty;
import com.dreameddeath.rating.model.cdr.CdrsBucketLink;

import java.util.Collections;
import java.util.List;

public final class StandardRatingContext extends RatingContext {
    @DocumentProperty("cdrsBucketLinks")
    private ListProperty<CdrsBucketLink> _cdrsBuckets=new ArrayListProperty<CdrsBucketLink>(StandardRatingContext.this);
    @DocumentProperty("guidingKeys")
    private ListProperty<RatingContextGuidingKey> _guidingKeys=new ArrayListProperty<RatingContextGuidingKey>(StandardRatingContext.this);
    @DocumentProperty("ratePlans")
    private ListProperty<RatingContextRatePlan> _ratePlans=new ArrayListProperty<RatingContextRatePlan>(StandardRatingContext.this);
    @DocumentProperty("sharedContexts")
    private ListProperty<RatingContextSharedLink> _sharedRatingCtxtLinks=new ArrayListProperty<RatingContextSharedLink>(StandardRatingContext.this);

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