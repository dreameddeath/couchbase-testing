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

import com.dreameddeath.core.couchbase.exception.StorageException;
import com.dreameddeath.core.dao.document.CouchbaseDocumentDao;
import com.dreameddeath.core.dao.exception.DaoException;
import com.dreameddeath.core.dao.exception.DaoNotFoundException;
import com.dreameddeath.core.dao.factory.CouchbaseDocumentDaoFactory;
import com.dreameddeath.core.dao.model.view.IViewQuery;
import com.dreameddeath.core.dao.model.view.IViewQueryResult;
import com.dreameddeath.core.dao.model.view.IViewQueryRow;
import com.dreameddeath.core.dao.session.ICouchbaseSession;
import com.dreameddeath.core.dao.session.ICouchbaseSessionFactory;
import com.dreameddeath.core.date.IDateTimeService;
import com.dreameddeath.core.java.utils.StringUtils;
import com.dreameddeath.core.json.model.Version;
import com.dreameddeath.core.user.IUser;
import com.dreameddeath.core.user.IUserFactory;
import com.dreameddeath.couchbase.core.catalog.config.CatalogConfigProperties;
import com.dreameddeath.couchbase.core.catalog.model.v1.Catalog;
import com.dreameddeath.couchbase.core.catalog.model.v1.CatalogElement;
import com.dreameddeath.couchbase.core.catalog.model.v1.view.CatalogViewResultValue;
import com.dreameddeath.couchbase.core.catalog.service.ICatalogRef;
import com.dreameddeath.couchbase.core.catalog.service.ICatalogService;
import com.github.benmanes.caffeine.cache.AsyncLoadingCache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import io.reactivex.schedulers.Schedulers;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static net.javacrumbs.futureconverter.java8rx2.FutureConverter.toCompletableFuture;

/**
 * Created by Christophe Jeunesse on 27/12/2017.
 */
public class CatalogService implements ICatalogService {
    public static final Pattern CACHE_SIZE_PATTERN = Pattern.compile("^\\s*(\\d+)\\s*([kKmMgG])?\\s*$");
    private final AsyncLoadingCache<Key,? extends CatalogElement> cache;
    private final String domain;
    private final Catalog.State state;
    private final Map<Class<? extends CatalogElement>,CouchbaseDocumentDao<? extends CatalogElement>> daoMap = Maps.newConcurrentMap();
    private final Map<String,ICatalogRef> catalogRefFromKey=Maps.newConcurrentMap();
    private final List<PreloadedCatalogInfo> preloadedCatalogsSorted =new ArrayList<>();

    private ICouchbaseSession session;
    private ICouchbaseSessionFactory sessionFactory;
    private IUserFactory userFactory;
    private CouchbaseDocumentDaoFactory factory;
    private IDateTimeService dateTimeService;

    protected static long getCacheSize(){
        String cache_size_str = CatalogConfigProperties.CATALOG_CACHE_SIZE.get();
        Matcher matcher = CACHE_SIZE_PATTERN.matcher(cache_size_str);
        Preconditions.checkArgument(matcher.matches(),"Not a valid format %s",cache_size_str);
        int raw_size = Integer.parseInt(matcher.group(1));
        if(matcher.groupCount()==2 && StringUtils.isNotEmpty(matcher.group(2))){
            String unit = matcher.group(2).toLowerCase();
            if(unit.equals("m")){
                raw_size *= 1024*1024;
            }
            else if(unit.equals("k")){
                raw_size *=1024;
            }
            else if(unit.equals("g")){
                raw_size *=1024*1024*1024;
            }
        }
        return raw_size;
    }

    public CatalogService() {
        this.domain = CatalogConfigProperties.CATALOG_DOMAIN.get();
        this.state = Catalog.State.valueOf(CatalogConfigProperties.CATALOG_STATE.get().toUpperCase());
        this.cache = Caffeine.newBuilder()
                .maximumWeight(getCacheSize())
                .recordStats()
                .buildAsync(this::asyncLoad);
    }

    @PostConstruct
    public void init() throws DaoException,StorageException{
        IUser user = userFactory.fromId(CatalogConfigProperties.CATALOG_USER_ID.get());
        session = sessionFactory.newReadOnlySession(this.domain,user);
        reloadCatalogs();
    }

    synchronized public void reloadCatalogs() throws DaoException, StorageException {
        IViewQuery<DateTime, CatalogViewResultValue, Catalog> allCatalogView = session.initViewQuery(Catalog.class, Catalog.ALL_CATALOG_VIEW_NAME);
        IViewQueryResult<DateTime, CatalogViewResultValue, Catalog> allCatalogViewResult = session.executeQuery(allCatalogView);
        List<PreloadedCatalogInfo> toRemoveList = Lists.newArrayList();
        List<PreloadedCatalogInfo> toAddList = Lists.newArrayList();

        for (IViewQueryRow<DateTime, CatalogViewResultValue, Catalog> resultRow : allCatalogViewResult.getAllRows()) {
            boolean needAdd=true;
            for(PreloadedCatalogInfo catalogInfo: preloadedCatalogsSorted){
                if(catalogInfo.state.equals(resultRow.getValue().state) && catalogInfo.dateTime.equals(resultRow.getKey())){
                    if(catalogInfo.version.compareTo(resultRow.getValue().version)<0){
                        toRemoveList.add(catalogInfo);
                    }
                    else{
                        needAdd = false;
                    }
                }
            }
            if(needAdd){
                toAddList.add(new PreloadedCatalogInfo(resultRow));
            }
        }
        preloadedCatalogsSorted.removeAll(toRemoveList);
        preloadedCatalogsSorted.addAll(toAddList);
        preloadedCatalogsSorted.sort(Comparator.<PreloadedCatalogInfo,DateTime>comparing(a -> a.dateTime).thenComparing(a->a.version));
        getCatalog();
    }

    @Autowired
    public void setSessionFactory(ICouchbaseSessionFactory factory){
        this.sessionFactory = factory;
    }

    @Autowired
    public void setUserFactory(IUserFactory factory){
        this.userFactory = factory;
    }

    @Autowired
    public void setFactory(CouchbaseDocumentDaoFactory factory) {
        this.factory = factory;
    }

    @Autowired
    public void setDateTimeService(IDateTimeService dateTimeService) {
        this.dateTimeService = dateTimeService;
    }

    private CompletableFuture<? extends CatalogElement> asyncLoad(Key key, Executor executor){
        return toCompletableFuture(
                daoMap.computeIfAbsent(key.elemClass, clazz->{
                    try {
                        return factory.getDaoForClass(domain, clazz);
                    }
                    catch (DaoNotFoundException e){
                        throw new RuntimeException(e);
                    }
                })
                .asyncGet(this.session, key.item_uid)
        .subscribeOn(Schedulers.from(executor)));
    }

    @Override
    public ICatalogRef getCatalog(DateTime date) {
        PreloadedCatalogInfo bestMatch=null;
        for (PreloadedCatalogInfo preloadedCatalog : preloadedCatalogsSorted) {
            if(bestMatch==null){
                bestMatch=preloadedCatalog;
            }
            else {
                if(bestMatch.state.equals(this.state)){
                    if(isBetterMatchingDateAndState(date, bestMatch, preloadedCatalog)){
                        bestMatch = preloadedCatalog;
                    }
                }
                else if(isBetterMatchingDate(date,bestMatch,preloadedCatalog)){
                    bestMatch = preloadedCatalog;
                }

            }
        }
        if(bestMatch!=null){
            return catalogRefFromKey.computeIfAbsent(bestMatch.catalogDocKey,key->new CatalogRefImpl(this,key,session));
        }
        return null;
    }

    public boolean isBetterMatchingDateAndState(DateTime requestedDateTime, PreloadedCatalogInfo bestMatch, PreloadedCatalogInfo preloadedCatalog) {
        return preloadedCatalog.state.equals(this.state) &&
                isBetterMatchingDate(requestedDateTime,bestMatch, preloadedCatalog);
    }

    private boolean isBetterMatchingDate(DateTime requestedDateTime, PreloadedCatalogInfo bestMatch, PreloadedCatalogInfo preloadedCatalog) {
        return bestMatch.dateTime.compareTo(preloadedCatalog.dateTime) <= 0
                && preloadedCatalog.dateTime.compareTo(requestedDateTime) <= 0;
    }

    @Override
    public ICatalogRef getCatalog() {
        return getCatalog(dateTimeService.getCurrentDate());
    }

    public void handleCatalogLoadingError(CatalogRefImpl catalogRef) {

    }

    private final class Key{
        final String item_uid;
        final Version item_version;
        final Class<? extends CatalogElement> elemClass;

        public Key(String item_uid, Version item_version, Class<? extends CatalogElement> elemClass) {
            this.item_uid = item_uid;
            this.item_version = item_version;
            this.elemClass = elemClass;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Key key = (Key) o;
            return Objects.equals(item_uid, key.item_uid) &&
                    Objects.equals(item_version, key.item_version) &&
                    Objects.equals(elemClass, key.elemClass);
        }

        @Override
        public int hashCode() {
            return Objects.hash(item_uid, item_version, elemClass);
        }
    }

    private final class PreloadedCatalogInfo {
        final DateTime dateTime;
        final Catalog.State state;
        final Version version;
        final String catalogDocKey;

        public PreloadedCatalogInfo(IViewQueryRow<DateTime, CatalogViewResultValue, Catalog> resultRow) {
            this.dateTime = resultRow.getKey();
            this.state = resultRow.getValue().state;
            this.version = resultRow.getValue().version;
            this.catalogDocKey = resultRow.getDocKey();
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            PreloadedCatalogInfo that = (PreloadedCatalogInfo) o;
            return Objects.equals(dateTime, that.dateTime) &&
                    state == that.state &&
                    Objects.equals(version, that.version) &&
                    Objects.equals(catalogDocKey, that.catalogDocKey);
        }

        @Override
        public int hashCode() {
            return Objects.hash(dateTime, state, version, catalogDocKey);
        }
    }
}

