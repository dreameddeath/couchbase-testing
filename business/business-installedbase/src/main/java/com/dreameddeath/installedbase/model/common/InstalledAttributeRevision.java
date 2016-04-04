package com.dreameddeath.installedbase.model.common;

import com.dreameddeath.core.model.annotation.DocumentProperty;
import com.dreameddeath.core.model.document.CouchbaseDocumentElement;
import com.dreameddeath.core.model.property.ListProperty;
import com.dreameddeath.core.model.property.Property;
import com.dreameddeath.core.model.property.impl.ArrayListProperty;
import com.dreameddeath.core.model.property.impl.StandardProperty;
import com.dreameddeath.core.validation.annotation.NotNull;

import java.util.Collection;
import java.util.List;

/**
 * Created by Christophe Jeunesse on 25/03/2016.
 */
public class InstalledAttributeRevision extends CouchbaseDocumentElement {
    /**
     *  code : The catalogue code of the attribute
     */
    @DocumentProperty("code") @NotNull
    private Property<String> code = new StandardProperty<>(InstalledAttributeRevision.this);

    /**
     *  action : Action on the value is precised
     */
    @DocumentProperty("action")
    private Property<Action> action = new StandardProperty<>(InstalledAttributeRevision.this);

    /**
     *  values : revisions of values
     */
    @DocumentProperty("values")
    private ListProperty<InstalledValueRevision> values = new ArrayListProperty<>(InstalledAttributeRevision.this);

    /**
     * Getter of code
     * @return the content
     */
    public String getCode() { return code.get(); }
    /**
     * Setter of code
     * @param val the new content
     */
    public void setCode(String val) { code.set(val); }
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

    /**
     * Getter of values
     * @return the content
     */
    public List<InstalledValueRevision> getValues() { return values.get(); }
    /**
     * Setter of values
     * @param vals the new collection of values
     */
    public void setValues(Collection<InstalledValueRevision> vals) { values.set(vals); }
    /**
     * Add a new entry to the property values
     * @param val the new entry to be added
     */
    public boolean addValues(InstalledValueRevision val){ return values.add(val); }
    /**
     * Remove an entry to the property values
     * @param val the entry to be remove
     * @return true if the entry has been removed
     */
    public boolean removeValues(InstalledValueRevision val){ return values.remove(val); }

    public boolean isSame(InstalledAttributeRevision target){
        return code.equals(target.code)
                && action.equals(target.action)
                && InstalledValueRevision.isSameRevisionList(values,target.values);
    }

    public static boolean isSameAttributeList(List<InstalledAttributeRevision> src,List<InstalledAttributeRevision> target){
        int nbTargetAttrsMatched=0;
        for(InstalledAttributeRevision srcAttrRev:src){
            boolean found=false;
            for(InstalledAttributeRevision targetAttrRev:target){
                if(srcAttrRev.getCode().equals(targetAttrRev.getCode())) {
                    ++nbTargetAttrsMatched;
                    if(!srcAttrRev.isSame(targetAttrRev)){
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

        return nbTargetAttrsMatched!=target.size();
    }

    public enum Action{
        ADD,
        REMOVE
    }

}
