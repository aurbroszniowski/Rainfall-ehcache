package io.rainfall.ehcache3.operation;

import io.rainfall.AssertionEvaluator;
import io.rainfall.Configuration;
import io.rainfall.ObjectGenerator;
import io.rainfall.Operation;
import io.rainfall.SequenceGenerator;
import io.rainfall.TestException;
import io.rainfall.ehcache3.CacheDefinition;
import io.rainfall.statistics.StatisticsHolder;
import org.ehcache.Cache;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

import static io.rainfall.ehcache.statistics.EhcacheResult.EXCEPTION;
import static io.rainfall.ehcache.statistics.EhcacheResult.PUTALL;

/**
 * @author Aurelien Broszniowski
 */
public class PutAllOperation<K, V> implements Operation {

  private final ObjectGenerator<K> keyGenerator;
  private final ObjectGenerator<V> valueGenerator;
  private final SequenceGenerator sequenceGenerator;
  private final int bulkBatchSize;
  private final Iterable<CacheDefinition<K, V>> cacheDefinitions;

  public PutAllOperation(final ObjectGenerator<K> keyGenerator, final ObjectGenerator<V> valueGenerator,
                         final SequenceGenerator sequenceGenerator, final int bulkBatchSize, final Iterable<CacheDefinition<K, V>> caches) {
    this.keyGenerator = keyGenerator;
    this.valueGenerator = valueGenerator;
    this.sequenceGenerator = sequenceGenerator;
    this.bulkBatchSize = bulkBatchSize;
    this.cacheDefinitions = caches;
  }

  @Override
  public void exec(final StatisticsHolder statisticsHolder, final Map<Class<? extends Configuration>,
      Configuration> configurations, final List<AssertionEvaluator> assertions) throws TestException {

    final long next = this.sequenceGenerator.next();
    Map<K, V> maps = new WeakHashMap<K, V>();
    for (int i = 0; i < bulkBatchSize; i++) {
      maps.put(keyGenerator.generate(next), valueGenerator.generate(next));
    }

    for (final CacheDefinition<K, V> cacheDefinition : cacheDefinitions) {
      Cache<K, V> cache = cacheDefinition.getCache();
      long start = statisticsHolder.getTimeInNs();
      try {
        cache.putAll(maps);
        long end = statisticsHolder.getTimeInNs();
        statisticsHolder.record(cacheDefinition.getName(), (end - start), PUTALL);
      } catch (Exception e) {
        long end = statisticsHolder.getTimeInNs();
        statisticsHolder.record(cacheDefinition.getName(), (end - start), EXCEPTION);
      }
    }
  }

  @Override
  public List<String> getDescription() {
    List<String> desc = new ArrayList<String>();
    desc.add("putAll(Map<? extends " + keyGenerator.getDescription() + ", ? extends "
             + valueGenerator.getDescription() + "> entries)");
    desc.add(sequenceGenerator.getDescription());
    return desc;
  }
}
