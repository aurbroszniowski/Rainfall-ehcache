package io.rainfall.ehcache3.operation;

import io.rainfall.AssertionEvaluator;
import io.rainfall.Configuration;
import io.rainfall.ObjectGenerator;
import io.rainfall.Operation;
import io.rainfall.SequenceGenerator;
import io.rainfall.TestException;
import io.rainfall.ehcache.operation.OperationWeight;
import io.rainfall.ehcache.statistics.EhcacheResult;
import io.rainfall.ehcache3.CacheConfig;
import io.rainfall.statistics.StatisticsHolder;
import io.rainfall.statistics.Task;
import org.ehcache.Cache;

import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

import static io.rainfall.ehcache.statistics.EhcacheResult.EXCEPTION;
import static io.rainfall.ehcache.statistics.EhcacheResult.PUT;

/**
 * @author Aurelien Broszniowski
 */
public class PutOperation<K, V> extends Operation {

  AtomicLong putcnt = new AtomicLong();

  @Override
  public void exec(final StatisticsHolder statisticsHolder, final Map<Class<? extends Configuration>,
      Configuration> configurations, final List<AssertionEvaluator> assertions) throws TestException {

    CacheConfig<K, V> cacheConfig = (CacheConfig<K, V>)configurations.get(CacheConfig.class);
    SequenceGenerator sequenceGenerator = cacheConfig.getSequenceGenerator();
    final long next = sequenceGenerator.next();
    Double weight = cacheConfig.getRandomizer().nextDouble(next);
    if (cacheConfig.getOperationWeights().get(weight) == OperationWeight.OPERATION.PUT) {
      List<Cache<K, V>> caches = cacheConfig.getCaches();
      final ObjectGenerator<K> keyGenerator = cacheConfig.getKeyGenerator();
      final ObjectGenerator<V> valueGenerator = cacheConfig.getValueGenerator();
      for (final Cache<K, V> cache : caches) {
        statisticsHolder
            .measure(cache.toString(), new Task() {

              @Override
              public EhcacheResult definition() throws Exception {
                try {
                  cache.put(keyGenerator.generate(next), valueGenerator.generate(next));
                } catch (Exception e) {
                  return EXCEPTION;
                }
                return PUT;
              }
            });
      }
    }

  }
}
