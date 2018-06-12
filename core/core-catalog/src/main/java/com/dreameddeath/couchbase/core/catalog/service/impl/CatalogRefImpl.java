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

import com.dreameddeath.core.json.model.Version;
import com.dreameddeath.core.model.entity.model.EntityDef;
import com.dreameddeath.core.model.entity.model.EntityModelId;
import com.dreameddeath.couchbase.core.catalog.model.v1.Catalog;
import com.dreameddeath.couchbase.core.catalog.model.v1.CatalogElement;
import com.dreameddeath.couchbase.core.catalog.model.v1.changeset.CatalogChangeSet;
import com.dreameddeath.couchbase.core.catalog.model.v1.changeset.ChangeSetItem;
import com.dreameddeath.couchbase.core.catalog.service.ICatalogRef;
import io.reactivex.Maybe;
import io.reactivex.Observable;
import io.reactivex.Single;
import io.reactivex.disposables.Disposable;
import io.reactivex.subjects.MaybeSubject;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

/**
 * Created by Christophe Jeunesse on 02/01/2018.
 */
public class CatalogRefImpl implements ICatalogRef {
    private final CatalogService parent;
    private final AtomicReference<Catalog> catalogAtomicReference=new AtomicReference<>();
    private final CountDownLatch catalogCountDownEffective=new CountDownLatch(1);
    private final AtomicReference<ICatalogRef> patchedCatalog=new AtomicReference<>();
    private final Map<ChangeSetItemKey,ChangeSetItem> catalogItemRefs = new HashMap<>();
    private final Disposable disposable;

    public CatalogRefImpl(CatalogService parent, Single<Catalog> catalog) {
        this.parent = parent;
        this.disposable = catalog.subscribe(this::setCatalog, this::manageError);
    }

    private void manageError(Throwable exception){
        parent.handleCatalogLoadingError(exception,this);
        this.disposable.dispose();
    }

    private synchronized void setCatalog(Catalog cat){
        this.catalogAtomicReference.set(cat);

        if(cat.getPatchedVersion()!=null) {
            this.patchedCatalog.set(this.parent.getCatalog(cat.getPatchedVersion()));
        }

        for (CatalogChangeSet changeSet: cat.getChangeSets()) {
            for(ChangeSetItem item :changeSet.getChanges()){
                this.catalogItemRefs.put(new ChangeSetItemKey(item.getId(),item.getTarget()),item);
            }
        }
        if(this.disposable!=null){
            this.disposable.dispose();
        }
        this.catalogCountDownEffective.countDown();
    }

    private synchronized Single<Catalog> getCatalog(){
        try {
            this.catalogCountDownEffective.await(10, TimeUnit.SECONDS);
        }
        catch (InterruptedException e){
            return Single.error(e);
        }
        return Single.just(this.catalogAtomicReference.get());
    }

    @Override
    public Single<String> getCatalogName() {
        return getCatalog().map(Catalog::getName);
    }

    @Override
    public Single<Version> getCatalogVersion() {
        return getCatalog().map(Catalog::getVersion);
    }

    @Override
    public Single<Catalog.State> getCatalogState() {
        return getCatalog().map(Catalog::getState);
    }

    @Override
    public Observable<ICatalogItemRef> getItems(){
        return this.getCatalog()
                .flatMapObservable(cat->(this.patchedCatalog.get()!=null)?this.patchedCatalog.get().getItems():Observable.empty())
                .toList()
                .flatMapObservable(this::mergeChangeSetItems);

    }

    private Observable<ICatalogItemRef> mergeChangeSetItems(List<ICatalogItemRef> previousList) {
        Set<ICatalogItemRef> result = new HashSet<>(previousList.size() + this.catalogItemRefs.size());

        result.addAll(
                this.catalogAtomicReference.get().getChangeSets().stream()
                        .flatMap(catalogChangeSet -> catalogChangeSet.getChanges().stream())
                        .map(this::buildCatalogItemRef)
                        .collect(Collectors.toSet())
        );
        return Observable.fromIterable(result);
    }

    private CatalogItemRefImpl buildCatalogItemRef(ChangeSetItem changeSetItem) {
        Class<? extends CatalogElement> clazz = this.parent.getEntityDefinitionManager().findClassFromVersionnedTypeId(changeSetItem.getTarget());
        return new CatalogItemRefImpl(changeSetItem.getId(), changeSetItem.getVersion(),changeSetItem.getKey(), clazz);
    }

    @Override
    public <T extends CatalogElement> Maybe<T> getCatalogElement(String uid, Class<T> type) {
        Maybe<T> localResult =
                this.getLocalChangeSetItem(uid,type)
                .flatMap(item->getCatalogElement(item,type));
        if(this.patchedCatalog.get()!=null){
            localResult = localResult.switchIfEmpty(this.patchedCatalog.get().getCatalogElement(uid,type));
        }

        return localResult;
    }

    private <T extends CatalogElement> Maybe<ChangeSetItem> getLocalChangeSetItem(String uid, Class<T> type){
        return this.getCatalog()
                .flatMapMaybe(cat-> {
                    ChangeSetItem changeSetItem = this.catalogItemRefs.get(new ChangeSetItemKey(uid, EntityDef.build(type).getModelId()));
                    if(changeSetItem!=null){
                        return Maybe.just(changeSetItem);
                    }
                    else{
                        return Maybe.empty();
                    }
                });
    }

    private <T extends CatalogElement> Maybe<T> getCatalogElement(ChangeSetItem item,Class<T> type) {
        MaybeSubject<T> result = MaybeSubject.create();
        CompletableFuture<T> tCompletableFuture = (CompletableFuture<T>) this.parent.getCache().get(this.parent.getKeyFromChangeSetItem(item));
        tCompletableFuture.whenComplete((elem , e)->{
            if(e!=null){
                result.onError(e);
            }
            else if(elem!=null){
                result.onSuccess(elem);
            }
            else{
                result.onComplete();
            }
        });
        return result;
    }

    private static class ChangeSetItemKey{
        private final String uid;
        private final EntityModelId modelId;

        public ChangeSetItemKey(String uid, EntityModelId modelId) {
            this.uid = uid;
            this.modelId = modelId;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            ChangeSetItemKey that = (ChangeSetItemKey) o;
            return Objects.equals(uid, that.uid) &&
                    Objects.equals(modelId, that.modelId);
        }

        @Override
        public int hashCode() {
            return Objects.hash(uid, modelId);
        }
    }

    private class CatalogItemRefImpl implements ICatalogItemRef{
        private final String id;
        private final Version version;
        private final String key;
        private final Class<? extends CatalogElement> clazz;

        public CatalogItemRefImpl(String id, Version version, String key,Class<? extends CatalogElement> clazz) {
            this.id = id;
            this.version = version;
            this.key = key;
            this.clazz = clazz;
        }

        @Override
        public String id() {
            return id;
        }

        @Override
        public Version version() {
            return version;
        }

        @Override
        public Class<? extends CatalogElement> clazz() {
            return clazz;
        }

        @Override
        public String key() {
            return key;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            CatalogItemRefImpl that = (CatalogItemRefImpl) o;
            return Objects.equals(id, that.id) &&
                    Objects.equals(version, that.version) &&
                    Objects.equals(clazz, that.clazz);
        }

        @Override
        public int hashCode() {
            return Objects.hash(id, version,clazz);
        }
    }
}
