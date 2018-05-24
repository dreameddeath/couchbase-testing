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

package com.dreameddeath.couchbase.core.catalog.model.v1;

import com.dreameddeath.core.dao.annotation.dao.DaoEntity;
import com.dreameddeath.core.dao.annotation.dao.View;
import com.dreameddeath.core.dao.annotation.dao.ViewKeyDef;
import com.dreameddeath.core.dao.annotation.dao.ViewValueDef;
import com.dreameddeath.core.dao.document.CouchbaseDocumentWithKeyPatternDao;
import com.dreameddeath.core.dao.model.view.impl.ViewDateTimeKeyTranscoder;
import com.dreameddeath.core.json.model.Version;
import com.dreameddeath.core.model.annotation.DocumentEntity;
import com.dreameddeath.core.model.annotation.DocumentProperty;
import com.dreameddeath.core.model.document.CouchbaseDocument;
import com.dreameddeath.core.model.property.ListProperty;
import com.dreameddeath.core.model.property.Property;
import com.dreameddeath.core.model.property.impl.ArrayListProperty;
import com.dreameddeath.core.model.property.impl.ImmutableProperty;
import com.dreameddeath.core.model.property.impl.StandardProperty;
import com.dreameddeath.couchbase.core.catalog.model.v1.changeset.CatalogChangeSet;
import com.dreameddeath.couchbase.core.catalog.model.v1.view.CatalogViewResultValue;
import com.dreameddeath.couchbase.core.catalog.service.v1.CatalogViewResultValueTranscoder;
import org.joda.time.DateTime;

import java.util.Collection;
import java.util.List;

/**
 * Created by Christophe Jeunesse on 14/12/2017.
 */
@DaoEntity(baseDao = CouchbaseDocumentWithKeyPatternDao.class,dbPath = "catalog",idFormat = "%10d",idPattern = "\\d{10}")
@View(name = Catalog.ALL_CATALOG_VIEW_NAME,
        content = "emit(dateToArray(doc.startTime),{state:doc.state,version:doc.version})",
        keyDef = @ViewKeyDef(type = DateTime.class,transcoder = ViewDateTimeKeyTranscoder.class),
        valueDef = @ViewValueDef(type = CatalogViewResultValue.class, transcoder = CatalogViewResultValueTranscoder.class)
)
@DocumentEntity
public class Catalog extends CouchbaseDocument {
    public static final String ALL_CATALOG_VIEW_NAME ="all_catalogs";
    /**
     * name : Catalog version
     */
    @DocumentProperty("name")
    private Property<String> name = new ImmutableProperty<>(Catalog.this);
    /**
     * version : Version of the catalogue
     */
    @DocumentProperty("version")
    private Property<Version> version = new ImmutableProperty<>(Catalog.this);

    /**
     * state : The applicable states of document
     */
    @DocumentProperty("state")
    private Property<State> state = new StandardProperty<>(Catalog.this);


    /**
     * startDate : the start validity date for the given catalog
     */
    @DocumentProperty("startDate")
    private Property<DateTime> startDate = new StandardProperty<>(Catalog.this);

    /**
     * changeSets : List of included change set
     */
    @DocumentProperty("changeSets")
    private ListProperty<CatalogChangeSet> changeSets = new ArrayListProperty<>(Catalog.this);
    /**
     * patchedVersions : List of patched version (if any)
     */
    @DocumentProperty("patchedVersions")
    private ListProperty<Version> patchedVersions = new ArrayListProperty<>(Catalog.this);

    /**
     * Getter of the attribute {@link #name}
     * @return the currentValue of {@link #name}
     */
    public String getName(){
        return this.name.get();
    }

    /**
     * Setter of the attribute {@link #name}
     * @param newValue the newValue of {@link #name}
     */
    public void setName(String newValue){
        this.name.set(newValue);
    }

    /**
     * Getter of the attribute {@link #version}
     * return the currentValue of {@link #version}
     */
    public Version getVersion(){
        return this.version.get();
    }

    /**
     * Setter of the attribute {@link #version}
     * @param newValue the newValue of {@link #version}
     */
    public void setVersion(Version newValue){
        this.version.set(newValue);
    }


    /**
     * Getter of the attribute {@link #startDate}
     * @return the currentValue of {@link #startDate}
     */
    public DateTime getStartDate(){
        return this.startDate.get();
    }

    /**
     * Setter of the attribute {@link #startDate}
     * @param newValue the newValue of {@link #startDate}
     */
    public void setStartDate(DateTime newValue){
        this.startDate.set(newValue);
    }


    /**
     * Getter of the attribute {@link #changeSets}
     * return the current list contained in {@link #changeSets}
     */
    public List<CatalogChangeSet> getChangeSets(){
        return this.changeSets;
    }

    /**
     * Replace the content of the attribute {@link #changeSets}
     * @param newContent the new content of {@link #changeSets}
     */
    public void setChangeSets(Collection<CatalogChangeSet> newContent){
        this.changeSets.set(newContent);
    }

    /**
     * Adds an item to the attribute {@link #changeSets}
     * @param newItem the new item to be added to {@link #changeSets}
     */
    public void addChangeSet(CatalogChangeSet newItem){
        this.changeSets.add(newItem);
    }

    /**
     * Getter of the attribute {@link #state}
     * @return the currentValue of {@link #state}
     */
    public State getState(){
        return this.state.get();
    }

    /**
     * Setter of the attribute {@link #state}
     * @param newValue the newValue of {@link #state}
     */
    public void setState(State newValue){
        this.state.set(newValue);
    }

    /**
     * Getter of the attribute {@link #patchedVersions}
     * return the current list contained in {@link #patchedVersions}
     */
    public List<Version> getPatchedVersions(){
        return this.patchedVersions;
    }

    /**
     * Replace the content of the attribute {@link #patchedVersions}
     * @param newContent the new content of {@link #patchedVersions}
     */
    public void setPatchedVersions(Collection<Version> newContent){
        this.patchedVersions.set(newContent);
    }

    /**
     * Adds an item to the attribute {@link #patchedVersions}
     * @param newItem the new item to be added to {@link #patchedVersions}
     */
    public void addPatchedVersion(Version newItem){
        this.patchedVersions.add(newItem);
    }


    public enum State{
        DEV,
        QUALIF,
        PREPROD,
        PROD
    }
}
