package com.dreameddeath.party.model;

import com.dreameddeath.common.annotation.DocumentProperty;

import com.dreameddeath.common.model.property.Property;
import com.dreameddeath.common.model.property.StandardProperty;

import java.util.List;

/**
 * Created by ceaj8230 on 01/08/2014.
 */
public class Person extends Party {
    @DocumentProperty("firstName")
    private Property<String> _firstName=new StandardProperty<String>(Person.this);
    @DocumentProperty("lastName")
    private Property<String> _lastName=new StandardProperty<String>(Person.this);

    public String getFirstName(){return _firstName.get();}
    public void setFirstName(String firstName){_firstName.set(firstName);}

    public String getLastName(){return _lastName.get();}
    public void setLastName(String lastName){_lastName.set(lastName);}
}
