package io.rainfall.ehcache3.operation;

import io.rainfall.AssertionEvaluator;
import io.rainfall.Configuration;
import io.rainfall.ObjectGenerator;
import io.rainfall.SequenceGenerator;
import io.rainfall.TestException;
import io.rainfall.ehcache3.CacheDefinition;
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

  public PutVerifiedOperation(final ObjectGenerator<K> keyGenerator, final ObjectGenerator<V> valueGenerator,
                              final SequenceGenerator sequenceGenerator, final Iterable<CacheDefinition<K, V>> caches) {
    super(keyGenerator, valueGenerator, sequenceGenerator, caches);
  }

  @Override
  public void exec(final StatisticsHolder statisticsHolder, final Map<Class<? extends Configuration>,
      Configuration> configurations, final List<AssertionEvaluator> assertions) throws TestException {

    final long next = this.sequenceGenerator.next();
    for (final CacheDefinition<K, V> cacheDefinition : cacheDefinitions) {
      Cache<K, V> cache = cacheDefinition.getCache();

      K k = keyGenerator.generate(next);
      V v = valueGenerator.generate(next);

      long start = statisticsHolder.getTimeInNs();
      try {
        cache.put(k, v);
        long end = statisticsHolder.getTimeInNs();
        statisticsHolder.record(cacheDefinition.getName(), (end - start), PUT);
      } catch (Exception e) {
        long end = statisticsHolder.getTimeInNs();
        statisticsHolder.record(cacheDefinition.getName(), (end - start), EXCEPTION);
      }

      V v1 = cache.get(k);
      if (v != null && !v.equals(v1)) {
        log.error("incorrect value found in cache for the key {} : {}", k, v);
        statisticsHolder.increaseAssertionsErrorsCount(cacheDefinition.getName());
      }
    }
  }

  @Override
  public List<String> getDescription() {
    List<String> desc = new ArrayList<String>();
    desc.add( "put(" + keyGenerator.getDescription() + " key, " + valueGenerator.getDescription() + " value) with ASSERTION");
    desc.add(sequenceGenerator.getDescription());
    return desc;
  }
}
