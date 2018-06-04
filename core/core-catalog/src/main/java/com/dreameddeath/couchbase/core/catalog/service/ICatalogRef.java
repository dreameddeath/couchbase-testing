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

package com.dreameddeath.couchbase.core.catalog.service;

import com.dreameddeath.core.json.model.Version;
import com.dreameddeath.couchbase.core.catalog.model.v1.Catalog;
import com.dreameddeath.couchbase.core.catalog.model.v1.CatalogElement;
import io.reactivex.Maybe;
import io.reactivex.Observable;
import io.reactivex.Single;

/**
 * Created by Christophe Jeunesse on 15/12/2017.
 */
public interface ICatalogRef {
    Single<String> getCatalogName();
    Single<Version> getCatalogVersion();
    Single<Catalog.State> getCatalogState();

    Observable<ICatalogItemRef> getItems();

    <T extends CatalogElement> Maybe<T> getCatalogElement(String uid, Class<T> type);

    interface ICatalogItemRef{
        String id();
        Version version();
        String key();
        Class<? extends CatalogElement> clazz();
    }
}
