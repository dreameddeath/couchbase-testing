package com.dreameddeath.party.model;

import com.dreameddeath.core.annotation.DocumentProperty;
import com.dreameddeath.core.model.property.Property;
import com.dreameddeath.core.model.property.StandardProperty;

/**
 * Created by Christophe Jeunesse on 01/08/2014.
 */
public class Organization extends Party {
    @DocumentProperty("tradingName")
    private Property<String> _tradingName=new StandardProperty<String>(Organization.this);
    @DocumentProperty("brand")
    private Property<String> _brand=new StandardProperty<String>(Organization.this);

    public String getTradingName(){return _tradingName.get();}
    public void setTradingName(String name){_tradingName.set(name);}

    public String getBrand(){return _brand.get();}
    public void setBrand(String name){_brand.set(name);}

}
