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

package com.dreameddeath.common.model;

import com.dreameddeath.core.model.annotation.DocumentProperty;
import com.dreameddeath.core.model.document.CouchbaseDocumentElement;
import com.dreameddeath.core.model.property.Property;
import com.dreameddeath.core.model.property.impl.StandardProperty;

/**
 * Created by Christophe Jeunesse on 11/08/2014.
 */
public class Address extends CouchbaseDocumentElement {
    /**
     *  apartmentNumber : the number of the apartement
     */
    @DocumentProperty("apartmentNumber")
    private Property<String> apartmentNumber = new StandardProperty<>(Address.this);
    /**
     *  buildingName :
     */
    @DocumentProperty("buildingName")
    private Property<String> buildingName = new StandardProperty<>(Address.this);
    /**
     *  houseType :
     */
    @DocumentProperty("houseType")
    private Property<String> houseType = new StandardProperty<>(Address.this);
    /**
     *  floorNumber :
     */
    @DocumentProperty("floorNumber")
    private Property<String> floorNumber = new StandardProperty<>(Address.this);
    /**
     *  staircaseNumber :
     */
    @DocumentProperty("staircaseNumber")
    private Property<String> staircaseNumber = new StandardProperty<>(Address.this);
    /**
     *  geoCode :
     */
    @DocumentProperty("geoCode")
    private Property<String> geoCode = new StandardProperty<>(Address.this);
    /**
     *  residenceName :
     */
    @DocumentProperty("residenceName")
    private Property<String> residenceName = new StandardProperty<>(Address.this);
    /**
     *  roadNumber :
     */
    @DocumentProperty("roadNumber")
    private Property<String> roadNumber = new StandardProperty<>(Address.this);
    /**
     *  roadLetter :
     */
    @DocumentProperty("roadLetter")
    private Property<String> roadLetter = new StandardProperty<>(Address.this);
    /**
     *  roadType :
     */
    @DocumentProperty("roadType")
    private Property<String> roadType = new StandardProperty<>(Address.this);
    /**
     *  roadName :
     */
    @DocumentProperty("roadName")
    private Property<String> roadName = new StandardProperty<>(Address.this);
    /**
     *  locality :
     */
    @DocumentProperty("locality")
    private Property<String> locality = new StandardProperty<>(Address.this);
    /**
     *  postalCode : Postal Code of the city
     */
    @DocumentProperty("postalCode")
    private Property<String> postalCode = new StandardProperty<>(Address.this);
    /**
     *  cityName : Name of the city
     */
    @DocumentProperty("cityName")
    private Property<String> cityName = new StandardProperty<>(Address.this);
    /**
     *  state :
     */
    @DocumentProperty("state")
    private Property<String> state = new StandardProperty<>(Address.this);
    /**
     *  countryName :
     */
    @DocumentProperty("countryName")
    private Property<String> countryName = new StandardProperty<>(Address.this);

    // apartmentNumber accessors
    public String getApartmentNumber() { return apartmentNumber.get(); }
    public void setApartmentNumber(String val) { apartmentNumber.set(val); }
    // buildingName accessors
    public String getBuildingName() { return buildingName.get(); }
    public void setBuildingName(String val) { buildingName.set(val); }
    // houseType accessors
    public String getHouseType() { return houseType.get(); }
    public void setHouseType(String val) { houseType.set(val); }
    // floorNumber accessors
    public String getFloorNumber() { return floorNumber.get(); }
    public void setFloorNumber(String val) { floorNumber.set(val); }
    // staircaseNumber accessors
    public String getStaircaseNumber() { return staircaseNumber.get(); }
    public void setStaircaseNumber(String val) { staircaseNumber.set(val); }
    // geoCode accessors
    public String getGeoCode() { return geoCode.get(); }
    public void setGeoCode(String val) { geoCode.set(val); }
    // residenceName accessors
    public String getResidenceName() { return residenceName.get(); }
    public void setResidenceName(String val) { residenceName.set(val); }
    // roadNumber accessors
    public String getRoadNumber() { return roadNumber.get(); }
    public void setRoadNumber(String val) { roadNumber.set(val); }
    // roadLetter accessors
    public String getRoadLetter() { return roadLetter.get(); }
    public void setRoadLetter(String val) { roadLetter.set(val); }
    // roadType accessors
    public String getRoadType() { return roadType.get(); }
    public void setRoadType(String val) { roadType.set(val); }
    // roadName accessors
    public String getRoadName() { return roadName.get(); }
    public void setRoadName(String val) { roadName.set(val); }
    // locality accessors
    public String getLocality() { return locality.get(); }
    public void setLocality(String val) { locality.set(val); }
    // postalCode accessors
    public String getPostalCode() { return postalCode.get(); }
    public void setPostalCode(String val) { postalCode.set(val); }
    // cityName accessors
    public String getCityName() { return cityName.get(); }
    public void setCityName(String val) { cityName.set(val); }
    // countryName accessors
    public String getCountryName() { return countryName.get(); }
    public void setCountryName(String val) { countryName.set(val); }

}
