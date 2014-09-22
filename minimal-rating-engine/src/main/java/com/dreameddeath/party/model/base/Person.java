package com.dreameddeath.party.model.base;

import com.dreameddeath.core.annotation.DocumentProperty;

import com.dreameddeath.core.annotation.NotNull;
import com.dreameddeath.core.annotation.Unique;
import com.dreameddeath.core.model.property.Property;
import com.dreameddeath.core.model.property.impl.StandardProperty;
import org.joda.time.DateTime;

/**
 * Created by Christophe Jeunesse on 01/08/2014.
 */
public class Person extends Party {
    @DocumentProperty("firstName") @NotNull
    private Property<String> _firstName=new StandardProperty<String>(Person.this);
    @DocumentProperty("lastName") @NotNull
    private Property<String> _lastName=new StandardProperty<String>(Person.this);
    @DocumentProperty("birthDate")
    private Property<DateTime> _birthDate = new StandardProperty<DateTime>(Person.this);

    public String getFirstName(){return _firstName.get();}
    public void setFirstName(String firstName){_firstName.set(firstName);}

    public String getLastName(){return _lastName.get();}
    public void setLastName(String lastName){_lastName.set(lastName);}

    public DateTime getBirthDate() { return _birthDate.get(); }
    public void setBirthDate(DateTime val) { _birthDate.set(val); }

}
