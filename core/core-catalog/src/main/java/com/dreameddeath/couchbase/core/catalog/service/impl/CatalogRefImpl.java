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

package com.dreameddeath.couchbase.core.catalog.service.impl;

import com.dreameddeath.core.dao.session.ICouchbaseSession;
import com.dreameddeath.core.json.model.Version;
import com.dreameddeath.couchbase.core.catalog.model.v1.Catalog;
import com.dreameddeath.couchbase.core.catalog.model.v1.CatalogElement;
import com.dreameddeath.couchbase.core.catalog.model.v1.changeset.CatalogChangeSet;
import com.dreameddeath.couchbase.core.catalog.service.ICatalogRef;
import io.reactivex.Single;

import java.util.Optional;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Created by Christophe Jeunesse on 02/01/2018.
 */
public class CatalogRefImpl implements ICatalogRef {
    private final CatalogService parent;
    private final Single<Catalog> temporaryCatalogRead;
    private final AtomicReference<String> name=new AtomicReference<>("loading");
    private final AtomicReference<Version> version=new AtomicReference<>(Version.EMPTY_VERSION);
    private final AtomicReference<Catalog.State> state=new AtomicReference<>();
    private final AtomicReference<Catalog> catalogAtomicReference=new AtomicReference<>();
    private final CountDownLatch catalogCountDownEffective=new CountDownLatch(1);

    public CatalogRefImpl(CatalogService parent, String catalogDocKey, ICouchbaseSession session) {
        this.parent = parent;
        this.temporaryCatalogRead = session.asyncGet(catalogDocKey,Catalog.class);
        this.temporaryCatalogRead.subscribe(this.catalogAtomicReference::set, this::manageError);
    }

    private void manageError(Throwable exception){
        parent.handleCatalogLoadingError(this);
    }

    private void setCatalog(Catalog cat){
        this.catalogAtomicReference.set(cat);
        this.name.set(this.catalogAtomicReference.get().getName());
        this.version.set(this.catalogAtomicReference.get().getVersion());
        this.state.set(this.catalogAtomicReference.get().getState());

        cat.getPatchedVersions();
        for (CatalogChangeSet changeSet: cat.getChangeSets()) {
            /*for(Map.Entry<String,List<ChangeSetItem>> changeSetItemEntry :changeSet.getDomainChanges()){

            }*/
        }
        this.catalogCountDownEffective.countDown();
    }

    private Single<Catalog> getCatalog(){
        try {
            this.catalogCountDownEffective.await(10, TimeUnit.SECONDS);
        }
        catch (InterruptedException e){
            return Single.error(e);
        }
        return Single.just(this.catalogAtomicReference.get());
    }

    @Override
    public String getCatalogName() {
        return name.get();
    }

    @Override
    public Version getCatalogVersion() {
        return version.get();
    }

    @Override
    public Catalog.State getCatalogState() {
        return state.get();
    }

    @Override
    public <T extends CatalogElement> Optional<T> getCatalogElement(String uid, Class<T> type) {
        return getCatalog().map(cat->this.);
    }
}
