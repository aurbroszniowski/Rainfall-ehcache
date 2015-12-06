package io.rainfall.ehcache3.operation;

import io.rainfall.AssertionEvaluator;
import io.rainfall.Configuration;
import io.rainfall.TestException;
import io.rainfall.ehcache3.CacheConfig;
import io.rainfall.statistics.StatisticsHolder;
import org.ehcache.Cache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static io.rainfall.ehcache.statistics.EhcacheResult.EXCEPTION;
import static io.rainfall.ehcache.statistics.EhcacheResult.PUT;

/**
 * @author Aurelien Broszniowski
 */
public class PutVerifiedOperation<K, V> extends PutOperation<K, V> {

  private static final Logger log = LoggerFactory.getLogger(PutOperation.class);

  @Override
  public void exec(final StatisticsHolder statisticsHolder, final Map<Class<? extends Configuration>,
      Configuration> configurations, final List<AssertionEvaluator> assertions) throws TestException {

    CacheConfig<K, V> cacheConfig = (CacheConfig<K, V>)configurations.get(CacheConfig.class);
    final long next = this.sequenceGenerator.next();
    List<Cache<K, V>> caches = cacheConfig.getCaches();
    for (final Cache<K, V> cache : caches) {
      K k = keyGenerator.generate(next);
      V v = valueGenerator.generate(next);

      long start = getTimeInNs();
      try {
        cache.put(k, v);
        long end = getTimeInNs();
        statisticsHolder.record(cacheConfig.getCacheName(cache), (end - start), PUT);
      } catch (Exception e) {
        long end = getTimeInNs();
        statisticsHolder.record(cacheConfig.getCacheName(cache), (end - start), EXCEPTION);
      }

      V v1 = cache.get(k);
      if (v != null && !v.equals(v1)) {
        log.error("incorrect value found in cache for the key {} : {}", k, v);
        statisticsHolder.increaseAssertionsErrorsCount(cacheConfig.getCacheName(cache));
      }
    }
  }

  @Override
  public List<String> getDescription() {
    List<String> desc = new ArrayList<String>();
    desc.add(getWeightInPercent() + "% put(" + keyGenerator.getDescription() + " key, " + valueGenerator.getDescription() + " value) with ASSERTION");
    desc.add(sequenceGenerator.getDescription());
    return desc;
  }
}
