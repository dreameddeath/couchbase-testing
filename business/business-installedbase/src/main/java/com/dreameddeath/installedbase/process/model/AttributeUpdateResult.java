package com.dreameddeath.installedbase.process.model;

import com.dreameddeath.core.business.model.VersionedDocumentElement;
import com.dreameddeath.core.model.annotation.DocumentEntity;
import com.dreameddeath.core.model.annotation.DocumentProperty;
import com.dreameddeath.core.model.property.ListProperty;
import com.dreameddeath.core.model.property.Property;
import com.dreameddeath.core.model.property.impl.ArrayListProperty;
import com.dreameddeath.core.model.property.impl.ImmutableProperty;
import com.dreameddeath.core.model.property.impl.StandardProperty;

import java.util.Collection;
import java.util.List;

/**
 * Created by Christophe Jeunesse on 22/03/2016.
 */
@DocumentEntity
public class AttributeUpdateResult extends VersionedDocumentElement {
    /**
     *  code : the updated attribute code
     */
    @DocumentProperty("code")
    private Property<String> code = new ImmutableProperty<>(AttributeUpdateResult.this);
    /**
     *  action : Action of the attribute
     */
    @DocumentProperty("action")
    private Property<Action> action = new StandardProperty<>(AttributeUpdateResult.this);
    /**
     *  values : the updates on values
     */
    @DocumentProperty("values")
    private ListProperty<ValueUpdateResult> values = new ArrayListProperty<>(AttributeUpdateResult.this);


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
    public List<ValueUpdateResult> getValues() { return values.get(); }
    /**
     * Setter of values
     * @param vals the new collection of values
     */
    public void setValues(Collection<ValueUpdateResult> vals) { values.set(vals); }
    /**
     * Add a new entry to the property values
     * @param val the new entry to be added
     */
    public boolean addValues(ValueUpdateResult val){ return values.add(val); }
    /**
     * Remove an entry to the property values
     * @param val the entry to be remove
     * @return true if the entry has been removed
     */
    public boolean removeValues(ValueUpdateResult val){ return values.remove(val); }

    public enum Action{
        ADD,
        MODIFY,
        UNCHANGE
    }
}
