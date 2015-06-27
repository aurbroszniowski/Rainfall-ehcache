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
    List<Cache<K, V>> caches = cacheConfig.getCaches();
    for (final Cache<K, V> cache : caches) {
      statisticsHolder.measure(cacheConfig.getCacheName(cache), new PutAllOperationFunction<K, V>(cache, next, keyGenerator, valueGenerator, bulkBatchSize));
    }
  }

}
