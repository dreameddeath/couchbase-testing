package com.dreameddeath.couchbase.core.catalog.service.impl;

import com.codahale.metrics.Counter;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Timer;
import com.github.benmanes.caffeine.cache.stats.CacheStats;
import com.github.benmanes.caffeine.cache.stats.StatsCounter;
import com.google.common.base.Preconditions;

import javax.annotation.Nonnull;

public class MetricStatsCounter implements StatsCounter {
    private final Counter hitCount;
    private final Counter missCount;
    private final Counter loadSuccessCount;
    private final Counter loadFailureCount;
    private final Timer totalLoadTime;
    private final Counter evictionCount;
    private final Counter evictionWeight;


    /**
     * Constructs an instance for use by a single cache.
     *
     * @param registry the registry of metric instances
     * @param metricsPrefix the prefix name for the metrics
     */
    public MetricStatsCounter(MetricRegistry registry, String metricsPrefix) {
        Preconditions.checkNotNull(metricsPrefix);
        hitCount = registry.counter(metricsPrefix + ".hits");
        missCount = registry.counter(metricsPrefix + ".misses");
        totalLoadTime = registry.timer(metricsPrefix + ".loads");
        loadSuccessCount = registry.counter(metricsPrefix + ".loads-success");
        loadFailureCount = registry.counter(metricsPrefix + ".loads-failure");
        evictionCount = registry.counter(metricsPrefix + ".evictions");
        evictionWeight = registry.counter(metricsPrefix + ".evictions-weight");
    }


    @Override
    public void recordHits(int i) {
        hitCount.inc(i);
    }

    @Override
    public void recordMisses(int i) {
        missCount.inc(i);
    }

    @Override
    public void recordLoadSuccess(long l) {
        loadSuccessCount.inc(l);
    }

    @Override
    public void recordLoadFailure(long l) {
        loadFailureCount.inc(l);
    }

    @Override
    public void recordEviction() {
        this.recordEviction(1);
    }

    @Nonnull
    @Override
    public CacheStats snapshot() {
        return new CacheStats(
                hitCount.getCount(),
                missCount.getCount(),
                loadSuccessCount.getCount(),
                loadFailureCount.getCount(),
                totalLoadTime.getCount(),
                evictionCount.getCount(),
                evictionWeight.getCount()
        );
    }

    @Override
    public void recordEviction(int weight) {
        this.evictionCount.inc();
        this.evictionWeight.inc(weight);
    }
}
