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

import com.codahale.metrics.MetricRegistry;
import com.dreameddeath.core.couchbase.impl.ReadParams;
import com.dreameddeath.core.dao.document.CouchbaseDocumentDao;
import com.dreameddeath.core.dao.exception.DaoException;
import com.dreameddeath.core.dao.exception.DaoNotFoundException;
import com.dreameddeath.core.dao.factory.CouchbaseDocumentDaoFactory;
import com.dreameddeath.core.dao.model.view.IViewQuery;
import com.dreameddeath.core.dao.model.view.IViewQueryResult;
import com.dreameddeath.core.dao.model.view.IViewQueryRow;
import com.dreameddeath.core.dao.view.CouchbaseViewDao;
import com.dreameddeath.core.date.IDateTimeService;
import com.dreameddeath.core.java.utils.StringUtils;
import com.dreameddeath.core.json.model.Version;
import com.dreameddeath.core.model.document.CouchbaseDocument;
import com.dreameddeath.core.model.entity.EntityDefinitionManager;
import com.dreameddeath.couchbase.core.catalog.config.CatalogConfigProperties;
import com.dreameddeath.couchbase.core.catalog.model.v1.Catalog;
import com.dreameddeath.couchbase.core.catalog.model.v1.CatalogElement;
import com.dreameddeath.couchbase.core.catalog.model.v1.changeset.ChangeSetItem;
import com.dreameddeath.couchbase.core.catalog.model.v1.view.CatalogViewResultValue;
import com.dreameddeath.couchbase.core.catalog.service.ICatalogRef;
import com.dreameddeath.couchbase.core.catalog.service.ICatalogService;
import com.github.benmanes.caffeine.cache.AsyncLoadingCache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import io.reactivex.Single;
import io.reactivex.schedulers.Schedulers;
import org.joda.time.DateTime;

import javax.annotation.Nonnull;
import javax.annotation.PostConstruct;
import java.util.*;
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
    private final ReadParams readParams;
    private final CouchbaseDocumentDaoFactory factory;
    private final CouchbaseDocumentDao<Catalog> daoCatalog;
    private final IDateTimeService dateTimeService;
    private final MetricRegistry metricRegistry;

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

    public CatalogService(String domain, CouchbaseDocumentDaoFactory daoFactory, IDateTimeService dateTimeService) {
        this(domain,null,daoFactory,dateTimeService,null);
    }

    public CatalogService(String domain, CouchbaseDocumentDaoFactory daoFactory, IDateTimeService dateTimeService, MetricRegistry registry) {
        this(domain,null,daoFactory,dateTimeService,registry);
    }

    public CatalogService(String domain,String keyPrefix, CouchbaseDocumentDaoFactory daoFactory, IDateTimeService dateTimeService){
        this(domain, keyPrefix,daoFactory, dateTimeService,null);
    }
    public CatalogService(String domain,String keyPrefix,CouchbaseDocumentDaoFactory daoFactory,IDateTimeService dateTimeService,MetricRegistry metricRegistry) {
        this.factory = daoFactory;
        this.dateTimeService = dateTimeService;
        this.metricRegistry = metricRegistry;
        this.readParams = ReadParams.builder().with(keyPrefix).create();
        this.domain = domain;
        this.state = Catalog.State.valueOf(CatalogConfigProperties.CATALOG_STATE.get().toUpperCase());
        final String cachePrefix= "catalog.cache."+domain;
        Caffeine<Object,Object> caffeineCacheDef = Caffeine.newBuilder()
                .maximumWeight(getCacheSize())
                .weigher((key, val) -> {
                    if (val instanceof CouchbaseDocument) {
                        return ((CouchbaseDocument) val).getBaseMeta().getDbSize();
                    } else {
                        return val.toString().length();
                    }
                });

        if(metricRegistry!=null){
            caffeineCacheDef = caffeineCacheDef.recordStats(() -> new MetricStatsCounter(metricRegistry, cachePrefix));
        }

        this.cache = caffeineCacheDef.buildAsync(this::asyncLoad);
        try {
            this.daoCatalog = factory.getDaoForClass(this.domain, Catalog.class);
        }
        catch (DaoNotFoundException e){
            throw new RuntimeException(e);
        }
    }

    @PostConstruct
    public void init() throws DaoException{
        reloadCatalogs();
    }


    synchronized public void reloadCatalogs() throws DaoException {
        CouchbaseViewDao<DateTime, CatalogViewResultValue, Catalog> viewDao = (CouchbaseViewDao<DateTime, CatalogViewResultValue, Catalog>) this.factory.getViewDaoFactory().getViewDaoFor(this.domain, Catalog.class, Catalog.ALL_CATALOG_VIEW_NAME);
        IViewQuery<DateTime, CatalogViewResultValue, Catalog> catalogIViewQuery = viewDao.buildViewQuery(this.readParams.getKeyPrefix());
        IViewQueryResult<DateTime, CatalogViewResultValue, Catalog> allCatalogViewResult = viewDao.query(null, true, catalogIViewQuery);
        List<PreloadedCatalogInfo> toRemoveList = Lists.newArrayList();
        List<PreloadedCatalogInfo> toAddList = Lists.newArrayList();

        for (IViewQueryRow<DateTime, CatalogViewResultValue, Catalog> resultRow : allCatalogViewResult.getAllRows()) {
            if(!this.domain.equals(resultRow.getValue().domain)){
                continue;
            }
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
        preloadedCatalogsSorted.sort(Comparator.<PreloadedCatalogInfo,Version>comparing(a -> a.version).thenComparing(a->a.dateTime).reversed());
        getCatalog();
    }



    private <T extends CatalogElement> CompletableFuture<T> asyncLoad(@Nonnull Key key, Executor executor){
        CouchbaseDocumentDao<T> couchbaseDocumentDao = (CouchbaseDocumentDao<T>)daoMap.computeIfAbsent(key.elemClass, clazz -> {
            try {
                return factory.getDaoForClass(domain, clazz);
            } catch (DaoNotFoundException e) {
                throw new RuntimeException(e);
            }
        });

        return toCompletableFuture(asyncRead(couchbaseDocumentDao,key.item_key).subscribeOn(Schedulers.from(executor)));
    }

    private Single<Catalog> asyncReadCatalogue(String key){
        return asyncRead(this.daoCatalog,key);
    }

    private <T extends CouchbaseDocument> Single<T> asyncRead(CouchbaseDocumentDao<T> dao,String key){
        return dao.getClient()
                .asyncGet(key,dao.getBaseClass(),this.readParams)
                .map(dao::managePostReading);
    }


    @Override
    public ICatalogRef getCatalog(DateTime date) {
        PreloadedCatalogInfo bestMatch=null;
        for (PreloadedCatalogInfo preloadedCatalog : preloadedCatalogsSorted) {
            if(bestMatch==null){
                bestMatch=preloadedCatalog;
            }
            else {
                if(isBetterState(bestMatch, preloadedCatalog)){
                    bestMatch = preloadedCatalog;
                }
                else if(isValidState(preloadedCatalog) && isBetterDate(date,bestMatch,preloadedCatalog)){
                    bestMatch=preloadedCatalog;
                }
            }
        }
        if(bestMatch!=null){
            String foundKey = bestMatch.catalogDocKey;
            return getCatalogRefFromKey(foundKey);
        }
        return null;
    }

    private boolean isBetterState(PreloadedCatalogInfo bestMatch, PreloadedCatalogInfo preloadedCatalog) {
        return bestMatch==null ||
                (!Objects.equals(this.state,bestMatch.state) && Objects.equals(this.state,preloadedCatalog.state));
    }

    private boolean isValidState(PreloadedCatalogInfo preloadedCatalog) {
        return this.state==null || Objects.equals(this.state,preloadedCatalog.state);
    }


    private boolean isBetterDate(DateTime date,PreloadedCatalogInfo bestMatch, PreloadedCatalogInfo preloadedCatalog) {
        return (bestMatch.dateTime==null || date.isAfter(bestMatch.dateTime)) && preloadedCatalog.dateTime!=null && !date.isAfter(preloadedCatalog.dateTime);
    }


    private ICatalogRef getCatalogRefFromKey(String foundKey) {
        return catalogRefFromKey.computeIfAbsent(foundKey,key->new CatalogRefImpl(this,this.asyncReadCatalogue(key)));
    }

    @Override
    public ICatalogRef getCatalog() {
        return getCatalog(dateTimeService.getCurrentDate());
    }

    @Override
    public ICatalogRef getCatalog(Version version) {
        for (PreloadedCatalogInfo preloadedCatalog : preloadedCatalogsSorted) {
            if(preloadedCatalog.version.equals(version)){
                return getCatalogRefFromKey(preloadedCatalog.catalogDocKey);
            }
        }
        return null;
    }


    public void handleCatalogLoadingError(Throwable e,CatalogRefImpl catalogRef) {

    }

    public EntityDefinitionManager getEntityDefinitionManager() {
        return this.factory.getEntityDefinitionManager();
    }

    public  AsyncLoadingCache<Key,? extends CatalogElement> getCache() {
        return this.cache;
    }

    public void cleanup() {
        //Nothing special
    }

    private final static class Key{
        final String item_uid;
        final Version item_version;
        final String item_key;
        final Class<? extends CatalogElement> elemClass;

        public Key(String item_uid, Version item_version,String key, Class<? extends CatalogElement> elemClass) {
            this.item_uid = item_uid;
            this.item_version = item_version;
            this.item_key = key;
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

    public Key getKeyFromChangeSetItem( ChangeSetItem item){
        Class<? extends CatalogElement> classFromVersionnedTypeId =this.factory.getEntityDefinitionManager().findClassFromVersionnedTypeId(item.getTarget());
        return new Key(item.getId(),item.getVersion(),item.getKey(),classFromVersionnedTypeId);
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

