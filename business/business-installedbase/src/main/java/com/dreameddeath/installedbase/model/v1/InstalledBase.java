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

package com.dreameddeath.installedbase.model.v1;

import com.dreameddeath.core.business.dao.BusinessCouchbaseDocumentDaoWithUID;
import com.dreameddeath.core.business.model.BusinessDocument;
import com.dreameddeath.core.dao.annotation.dao.Counter;
import com.dreameddeath.core.dao.annotation.dao.DaoEntity;
import com.dreameddeath.core.dao.annotation.dao.UidDef;
import com.dreameddeath.core.model.annotation.DocumentEntity;
import com.dreameddeath.core.model.annotation.DocumentProperty;
import com.dreameddeath.core.model.dto.annotation.processor.model.FieldGenMode;
import com.dreameddeath.core.model.dto.annotation.processor.model.SuperClassGenMode;
import com.dreameddeath.core.model.property.ListProperty;
import com.dreameddeath.core.model.property.Property;
import com.dreameddeath.core.model.property.impl.ArrayListProperty;
import com.dreameddeath.core.model.property.impl.ImmutableProperty;
import com.dreameddeath.core.model.property.impl.StandardProperty;
import com.dreameddeath.core.query.annotation.QueryExpose;
import com.dreameddeath.installedbase.model.EntityConstants;
import com.dreameddeath.installedbase.model.v1.contract.InstalledContract;
import com.dreameddeath.installedbase.model.v1.offer.InstalledOffer;
import com.dreameddeath.installedbase.model.v1.productservice.InstalledProductService;

import java.util.Collection;
import java.util.List;

/**
 * Created by Christophe Jeunesse on 10/08/2014.
 */
@DaoEntity(baseDao= BusinessCouchbaseDocumentDaoWithUID.class,dbPath = "instBase/",idPattern = "\\d{10}",idFormat = "%010d")
@Counter(name = "cnt",dbName = "cnt",isKeyGen = true)
@UidDef(fieldName = "uid")
@DocumentEntity(domain = EntityConstants.INSTALLED_BASE_DOMAIN)
@QueryExpose(rootPath = "installedBase",superClassGenMode = SuperClassGenMode.UNWRAP,defaultOutputFieldMode = FieldGenMode.SIMPLE)
public class InstalledBase extends BusinessDocument {
    /**
     *  uid : The unique id of the installed base
     */
    @DocumentProperty("uid")
    private Property<String> uid = new StandardProperty<>(InstalledBase.this);
    /**
     *  contract : The installed Contract for this installed base (if any)
     */
    @DocumentProperty("contract")
    private Property<InstalledContract> contract = new ImmutableProperty<>(InstalledBase.this);
    /**
     *  offers : List of offers being installed
     */
    @DocumentProperty("offers")
    private ListProperty<InstalledOffer> offers = new ArrayListProperty<>(InstalledBase.this);

    /**
     *  ps : Product services
     */
    @DocumentProperty("ps")
    private ListProperty<InstalledProductService> psList = new ArrayListProperty<>(InstalledBase.this);

    // uid accessors
    public String getUid() { return uid.get(); }
    public void setUid(String val) { uid.set(val); }

    // contract accessors
    public InstalledContract getContract() { return contract.get(); }
    public void setContract(InstalledContract val) { contract.set(val); }

    // Offers Accessors
    public List<InstalledOffer> getOffers() { return offers.get(); }
    public void setOffers(Collection<InstalledOffer> vals) { offers.set(vals); }
    public boolean addOffers(InstalledOffer val){ return offers.add(val); }
    public boolean removeOffers(InstalledOffer val){ return offers.remove(val); }

    /**
     * Getter of ps
     * @return the content
     */
    public List<InstalledProductService> getPsList() { return psList.get(); }
    /**
     * Setter of ps
     * @param vals the new collection of values
     */
    public void setPsList(Collection<InstalledProductService> vals) { psList.set(vals); }
    /**
     * Add a new entry to the property ps
     * @param val the new entry to be added
     */
    public boolean addPs(InstalledProductService val){ return psList.add(val); }
    /**
     * Remove an entry to the property ps
     * @param val the entry to be remove
     * @return true if the entry has been removed
     */
    public boolean removePs(InstalledProductService val){ return psList.remove(val); }

}
