/*
 * 	Copyright Christophe Jeunesse
 *
 * 	Licensed under the Apache License, Version 2.0 (the "License");
 * 	you may not use this file except in compliance with the License.
 * 	You may obtain a copy of the License at
 *
 * 	http://www.apache.org/licenses/LICENSE-2.0
 *
 * 	Unless required by applicable law or agreed to in writing, software
 * 	distributed under the License is distributed on an "AS IS" BASIS,
 * 	WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * 	See the License for the specific language governing permissions and
 * 	limitations under the License.
 *
 */

package com.dreameddeath.installedbase.model.v1.common;

import com.dreameddeath.core.model.annotation.DocumentProperty;
import com.dreameddeath.core.model.document.CouchbaseDocumentElement;
import com.dreameddeath.core.model.dto.annotation.processor.model.FieldGenMode;
import com.dreameddeath.core.model.dto.annotation.processor.model.SuperClassGenMode;
import com.dreameddeath.core.model.property.Property;
import com.dreameddeath.core.model.property.impl.ImmutableProperty;
import com.dreameddeath.core.model.property.impl.StandardProperty;
import com.dreameddeath.core.query.annotation.QueryExpose;
import org.joda.time.DateTime;

import java.util.List;

/**
 * Created by Christophe Jeunesse on 25/03/2016.
 */
@QueryExpose(rootPath = "dummy",isClassRootHierarchy = true,notDirecltyExposed = true,defaultOutputFieldMode = FieldGenMode.SIMPLE,superClassGenMode = SuperClassGenMode.UNWRAP)
public class InstalledItemLinkRevision extends CouchbaseDocumentElement {
    /**
     *  targetId : The target item id
     */
    @DocumentProperty("targetId")
    private Property<String> targetId = new ImmutableProperty<>(InstalledItemLinkRevision.this);
    /**
     *  type : The type of link
     */
    @DocumentProperty("type")
    private Property<InstalledItemLink.Type> type = new ImmutableProperty<>(InstalledItemLinkRevision.this);
    /**
     *  isReverse : if it's the reverse link. Default false
     */
    @DocumentProperty(value = "isReverse",getter = "isReverse",setter = "isReverse")
    private Property<Boolean> isReverse = new ImmutableProperty<>(InstalledItemLinkRevision.this);
    /**
     *  status : the revision status if any
     */
    @DocumentProperty("status")
    private Property<InstalledStatus.Code> status = new StandardProperty<>(InstalledItemLinkRevision.this);
    /**
     *  statusDate : The status effective date if different from the revision date
     */
    @DocumentProperty("statusDate")
    private Property<DateTime> statusDate = new StandardProperty<>(InstalledItemLinkRevision.this);
    /**
     *  action : The action on the link if any
     */
    @DocumentProperty("action")
    private Property<InstalledBaseRevisionAction> action = new StandardProperty<>(InstalledItemLinkRevision.this);

    /**
     * Getter of targetId
     * @return the content
     */
    public String getTargetId() { return targetId.get(); }
    /**
     * Setter of targetId
     * @param val the new content
     */
    public void setTargetId(String val) { targetId.set(val); }

    /**
     * Getter of type
     * @return the content
     */
    public InstalledItemLink.Type getType() { return type.get(); }
    /**
     * Setter of type
     * @param val the new content
     */
    public void setType(InstalledItemLink.Type val) { type.set(val); }
    /**
     * Getter of isReverse
     * @return the value of isReverse
     */
    public Boolean isReverse() { return isReverse.get(); }
    /**
     * Setter of isReverse
     * @param val the new value for isReverse
     */
    public void isReverse(Boolean val) { isReverse.set(val); }
    /**
     * Getter of status
     * @return the content
     */
    public InstalledStatus.Code getStatus() { return status.get(); }
    /**
     * Setter of status
     * @param val the new content
     */
    public void setStatus(InstalledStatus.Code val) { status.set(val); }
    /**
     * Getter of statusDate
     * @return the content
     */
    public DateTime getStatusDate() { return statusDate.get(); }
    /**
     * Setter of statusDate
     * @param val the new content
     */
    public void setStatusDate(DateTime val) { statusDate.set(val); }
    /**
     * Getter of action
     * @return the content
     */
    public InstalledBaseRevisionAction getAction() { return action.get(); }
    /**
     * Setter of action
     * @param val the new content
     */
    public void setAction(InstalledBaseRevisionAction val) { action.set(val); }


    @Override
    public String toString(){
        boolean reverse=(isReverse.get()!=null)&&(isReverse.get());
        return type+"/"+(reverse?"from":"to")+"/"+targetId;
    }

    public static boolean isSame(InstalledItemLinkRevision source,InstalledItemLinkRevision target){
        return source.targetId.equals(target.targetId)
                && source.type.equals(target.type)
                && source.isReverse.equals(target.isReverse)
                && source.status.equals(target.status)
                && source.action.equals(target.action);
    }


    public static boolean isSameLinkList(List<InstalledItemLinkRevision> srcList,List<InstalledItemLinkRevision> targetList){
        int nbTargetLinksMatched=0;
        for(InstalledItemLinkRevision link:srcList){
            boolean found=false;
            for(InstalledItemLinkRevision targetLink:targetList){
                if(
                        link.getTargetId().equals(targetLink.getTargetId())
                        && link.getType().equals(targetLink.getType())
                        && link.isReverse.equals(targetLink.isReverse)
                  )
                {
                    ++nbTargetLinksMatched;
                    if(!isSame(link,targetLink)){
                        return false;
                    }
                    else{
                        found=true;
                        break;
                    }
                }
            }
            if(!found){
                return  false;
            }
        }

        return nbTargetLinksMatched!=targetList.size();
    }
}
