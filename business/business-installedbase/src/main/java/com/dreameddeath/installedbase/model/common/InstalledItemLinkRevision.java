package com.dreameddeath.installedbase.model.common;

import com.dreameddeath.core.model.annotation.DocumentProperty;
import com.dreameddeath.core.model.document.CouchbaseDocumentElement;
import com.dreameddeath.core.model.property.Property;
import com.dreameddeath.core.model.property.impl.ImmutableProperty;
import com.dreameddeath.core.model.property.impl.StandardProperty;

import java.util.List;

/**
 * Created by Christophe Jeunesse on 25/03/2016.
 */
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
     *  direction : direction of the link
     */
    @DocumentProperty("direction")
    private Property<InstalledItemLink.Direction> direction = new ImmutableProperty<>(InstalledItemLinkRevision.this);
    /**
     *  status : the revision status if any
     */
    @DocumentProperty("status")
    private Property<InstalledStatus> status = new StandardProperty<>(InstalledItemLinkRevision.this);
    /**
     *  action : The action on the link if any
     */
    @DocumentProperty("action")
    private Property<Action> action = new StandardProperty<>(InstalledItemLinkRevision.this);

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
     * Getter of direction
     * @return the content
     */
    public InstalledItemLink.Direction getDirection() { return direction.get(); }
    /**
     * Setter of direction
     * @param val the new content
     */
    public void setDirection(InstalledItemLink.Direction val) { direction.set(val); }
    /**
     * Getter of status
     * @return the content
     */
    public InstalledStatus getStatus() { return status.get(); }
    /**
     * Setter of status
     * @param val the new content
     */
    public void setStatus(InstalledStatus val) { status.set(val); }
    /**
     * Getter of action
     * @return the content
     */
    public Action getAction() { return action.get(); }
    /**
     * Setter of action
     * @param val the new content
     */
    public void setAction(Action val) { action.set(val); }

    public boolean isSame(InstalledItemLinkRevision target){
        return targetId.equals(target.targetId)
                && type.equals(target.type)
                && direction.equals(target.direction)
                && status.equals(target.status)
                && action.equals(target.action)
                ;
    }

    @Override
    public String toString(){
        return direction+"/"+targetId;
    }

    public static boolean isSameLinkList(List<InstalledItemLinkRevision> srcList,List<InstalledItemLinkRevision> targetList){
        int nbTargetLinksMatched=0;
        for(InstalledItemLinkRevision link:srcList){
            boolean found=false;
            for(InstalledItemLinkRevision targetLink:targetList){
                if(
                        link.getTargetId().equals(targetLink.getTargetId())
                                && link.getType().equals(targetLink.getType())
                                && link.getDirection().equals(targetLink.getDirection())
                        )
                {
                    ++nbTargetLinksMatched;
                    if(!link.isSame(targetLink)){
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

    public enum Action{
        ADD,
        REMOVE,
        CHANGE
    }
}
