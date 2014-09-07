package com.dreameddeath.common.model;

import com.dreameddeath.core.annotation.DocumentProperty;
import com.dreameddeath.core.model.document.CouchbaseDocumentElement;
import com.dreameddeath.core.model.property.Property;
import com.dreameddeath.core.model.property.impl.StandardProperty;

/**
 * Created by ceaj8230 on 11/08/2014.
 */
public class Address extends CouchbaseDocumentElement {
    /**
     *  apartmentNumber : the number of the apartement
     */
    @DocumentProperty("apartmentNumber")
    private Property<String> _apartmentNumber = new StandardProperty<String>(Address.this);
    /**
     *  buildingName :
     */
    @DocumentProperty("buildingName")
    private Property<String> _buildingName = new StandardProperty<String>(Address.this);
    /**
     *  houseType :
     */
    @DocumentProperty("houseType")
    private Property<String> _houseType = new StandardProperty<String>(Address.this);
    /**
     *  floorNumber :
     */
    @DocumentProperty("floorNumber")
    private Property<String> _floorNumber = new StandardProperty<String>(Address.this);
    /**
     *  staircaseNumber :
     */
    @DocumentProperty("staircaseNumber")
    private Property<String> _staircaseNumber = new StandardProperty<String>(Address.this);
    /**
     *  geoCode :
     */
    @DocumentProperty("geoCode")
    private Property<String> _geoCode = new StandardProperty<String>(Address.this);
    /**
     *  residenceName :
     */
    @DocumentProperty("residenceName")
    private Property<String> _residenceName = new StandardProperty<String>(Address.this);
    /**
     *  roadNumber :
     */
    @DocumentProperty("roadNumber")
    private Property<String> _roadNumber = new StandardProperty<String>(Address.this);
    /**
     *  roadLetter :
     */
    @DocumentProperty("roadLetter")
    private Property<String> _roadLetter = new StandardProperty<String>(Address.this);
    /**
     *  roadType :
     */
    @DocumentProperty("roadType")
    private Property<String> _roadType = new StandardProperty<String>(Address.this);
    /**
     *  roadName :
     */
    @DocumentProperty("roadName")
    private Property<String> _roadName = new StandardProperty<String>(Address.this);
    /**
     *  locality :
     */
    @DocumentProperty("locality")
    private Property<String> _locality = new StandardProperty<String>(Address.this);
    /**
     *  postalCode : Postal Code of the city
     */
    @DocumentProperty("postalCode")
    private Property<String> _postalCode = new StandardProperty<String>(Address.this);
    /**
     *  cityName : Name of the city
     */
    @DocumentProperty("cityName")
    private Property<String> _cityName = new StandardProperty<String>(Address.this);
    /**
     *  state :
     */
    @DocumentProperty("state")
    private Property<String> _state = new StandardProperty<String>(Address.this);
    /**
     *  countryName :
     */
    @DocumentProperty("countryName")
    private Property<String> _countryName = new StandardProperty<String>(Address.this);

    // apartmentNumber accessors
    public String getApartmentNumber() { return _apartmentNumber.get(); }
    public void setApartmentNumber(String val) { _apartmentNumber.set(val); }
    // buildingName accessors
    public String getBuildingName() { return _buildingName.get(); }
    public void setBuildingName(String val) { _buildingName.set(val); }
    // houseType accessors
    public String getHouseType() { return _houseType.get(); }
    public void setHouseType(String val) { _houseType.set(val); }
    // floorNumber accessors
    public String getFloorNumber() { return _floorNumber.get(); }
    public void setFloorNumber(String val) { _floorNumber.set(val); }
    // staircaseNumber accessors
    public String getStaircaseNumber() { return _staircaseNumber.get(); }
    public void setStaircaseNumber(String val) { _staircaseNumber.set(val); }
    // geoCode accessors
    public String getGeoCode() { return _geoCode.get(); }
    public void setGeoCode(String val) { _geoCode.set(val); }
    // residenceName accessors
    public String getResidenceName() { return _residenceName.get(); }
    public void setResidenceName(String val) { _residenceName.set(val); }
    // roadNumber accessors
    public String getRoadNumber() { return _roadNumber.get(); }
    public void setRoadNumber(String val) { _roadNumber.set(val); }
    // roadLetter accessors
    public String getRoadLetter() { return _roadLetter.get(); }
    public void setRoadLetter(String val) { _roadLetter.set(val); }
    // roadType accessors
    public String getRoadType() { return _roadType.get(); }
    public void setRoadType(String val) { _roadType.set(val); }
    // roadName accessors
    public String getRoadName() { return _roadName.get(); }
    public void setRoadName(String val) { _roadName.set(val); }
    // locality accessors
    public String getLocality() { return _locality.get(); }
    public void setLocality(String val) { _locality.set(val); }
    // postalCode accessors
    public String getPostalCode() { return _postalCode.get(); }
    public void setPostalCode(String val) { _postalCode.set(val); }
    // cityName accessors
    public String getCityName() { return _cityName.get(); }
    public void setCityName(String val) { _cityName.set(val); }
    // countryName accessors
    public String getCountryName() { return _countryName.get(); }
    public void setCountryName(String val) { _countryName.set(val); }

}
