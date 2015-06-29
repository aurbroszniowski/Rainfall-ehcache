package io.rainfall.ehcache3.operation;

import io.rainfall.AssertionEvaluator;
import io.rainfall.Configuration;
import io.rainfall.EhcacheOperation;
import io.rainfall.TestException;
import io.rainfall.ehcache3.CacheConfig;
import io.rainfall.statistics.StatisticsHolder;
import org.ehcache.Cache;

import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

import static io.rainfall.ehcache.statistics.EhcacheResult.EXCEPTION;
import static io.rainfall.ehcache.statistics.EhcacheResult.PUTALL;

/**
 * @author Aurelien Broszniowski
 */
public class PutAllOperation<K, V> extends EhcacheOperation<K, V> {

  @Override
  public void exec(final StatisticsHolder statisticsHolder, final Map<Class<? extends Configuration>,
      Configuration> configurations, final List<AssertionEvaluator> assertions) throws TestException {

    CacheConfig<K, V> cacheConfig = (CacheConfig<K, V>)configurations.get(CacheConfig.class);
    int bulkBatchSize = cacheConfig.getBulkBatchSize();
    final long next = this.sequenceGenerator.next();
    Map<K, V> maps = new WeakHashMap<K, V>();
    for (int i = 0; i < bulkBatchSize; i++) {
      maps.put(keyGenerator.generate(next), valueGenerator.generate(next));
    }

    List<Cache<K, V>> caches = cacheConfig.getCaches();
    for (final Cache<K, V> cache : caches) {
      long start = getTimeInNs();
      try {
        cache.putAll(maps);
        long end = getTimeInNs();
        statisticsHolder.record(cacheConfig.getCacheName(cache), (end - start), PUTALL);
      } catch (Exception e) {
        long end = getTimeInNs();
        statisticsHolder.record(cacheConfig.getCacheName(cache), (end - start), EXCEPTION);
      }
    }
  }
}
