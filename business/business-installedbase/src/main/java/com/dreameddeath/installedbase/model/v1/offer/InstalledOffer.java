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

package com.dreameddeath.installedbase.model.v1.offer;

import com.dreameddeath.core.model.annotation.DocumentProperty;
import com.dreameddeath.core.model.dto.annotation.processor.model.FieldGenMode;
import com.dreameddeath.core.model.dto.annotation.processor.model.SuperClassGenMode;
import com.dreameddeath.core.model.entity.model.EntityModelId;
import com.dreameddeath.core.model.entity.model.IVersionedEntity;
import com.dreameddeath.core.model.property.ListProperty;
import com.dreameddeath.core.model.property.Property;
import com.dreameddeath.core.model.property.impl.ArrayListProperty;
import com.dreameddeath.core.model.property.impl.StandardProperty;
import com.dreameddeath.core.query.annotation.QueryExpose;
import com.dreameddeath.core.transcoder.json.CouchbaseDocumentTypeIdResolver;
import com.dreameddeath.installedbase.model.v1.common.IHasInstalledItemLink;
import com.dreameddeath.installedbase.model.v1.common.InstalledItem;
import com.dreameddeath.installedbase.model.v1.tariff.InstalledTariff;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.annotation.JsonTypeIdResolver;

import java.util.Collection;
import java.util.List;

/**
 * Created by Christophe Jeunesse on 10/08/2014.
 */
@JsonTypeInfo(use= JsonTypeInfo.Id.CUSTOM, include= JsonTypeInfo.As.PROPERTY, property="@t",visible = true)
@JsonTypeIdResolver(CouchbaseDocumentTypeIdResolver.class)
@QueryExpose(rootPath = "dummy",isClassRootHierarchy = true,notDirecltyExposed = true, defaultOutputFieldMode = FieldGenMode.SIMPLE,superClassGenMode = SuperClassGenMode.UNWRAP, forceGenerateMode = true)
public abstract class InstalledOffer extends InstalledItem<InstalledOfferRevision> implements IHasInstalledItemLink<InstalledOfferLink>, IVersionedEntity {
    private EntityModelId fullEntityId;
    @JsonSetter("@t") @Override
    public final void setDocumentFullVersionId(String typeId){
        fullEntityId = EntityModelId.build(typeId);
    }
    @Override
    public final String getDocumentFullVersionId(){
        return fullEntityId!=null?fullEntityId.toString():null;
    }
    @Override
    public final EntityModelId getModelId(){
        return fullEntityId;
    }

    /**
     *  parent : the current parent of the offer
     */
    @DocumentProperty("parent")
    private Property<String> parent = new StandardProperty<>(InstalledOffer.this);
    @DocumentProperty("links")
    private ListProperty<InstalledOfferLink> links = new ArrayListProperty<>(InstalledOffer.this);
    @DocumentProperty("tariffs")
    private ListProperty<InstalledTariff> tariffs = new ArrayListProperty<>(InstalledOffer.this);
    /**
     *  commercialParameters : explain the commercial parameters defined for the given offer
     */
    @DocumentProperty("commercialParameters")
    private ListProperty<InstalledCommercialParameter> commercialParameters = new ArrayListProperty<>(InstalledOffer.this);

    // links accessors
    @Override
    public List<InstalledOfferLink> getLinks() { return links.get(); }
    @Override
    public void setLinks(Collection<InstalledOfferLink> vals) { links.set(vals); }
    @Override
    public boolean addLink(InstalledOfferLink val){ return links.add(val); }
    @Override
    public boolean removeLink(InstalledOfferLink val){ return links.remove(val); }

    // tariffs accessors
    public List<InstalledTariff> getTariffs() { return tariffs.get(); }
    public void setTariffs(Collection<InstalledTariff> vals) { tariffs.set(vals); }
    public boolean addTariff(InstalledTariff val){ return tariffs.add(val); }
    public boolean removeTariff(InstalledTariff val){ return tariffs.remove(val); }

    // CommercialParameters Accessors
    public List<InstalledCommercialParameter> getCommercialParameters() { return commercialParameters.get(); }
    public void setCommercialParameters(Collection<InstalledCommercialParameter> vals) { commercialParameters.set(vals); }
    public boolean addCommercialParameter(InstalledCommercialParameter val){ return commercialParameters.add(val); }
    public boolean removeCommercialParameter(InstalledCommercialParameter val){ return commercialParameters.remove(val); }

    /**
     * Getter of parent
     * @return the content
     */
    public String getParent() { return parent.get(); }
    /**
     * Setter of parent
     * @param val the new content
     */
    public void setParent(String val) { parent.set(val); }
}
