package io.rainfall.ehcache2.operation;

import net.sf.ehcache.Ehcache;
import net.sf.ehcache.Element;
import io.rainfall.AssertionEvaluator;
import io.rainfall.Configuration;
import io.rainfall.ObjectGenerator;
import io.rainfall.Operation;
import io.rainfall.SequenceGenerator;
import io.rainfall.TestException;
import io.rainfall.ehcache.operation.OperationWeight;
import io.rainfall.ehcache.statistics.EhcacheResult;
import io.rainfall.ehcache2.CacheConfig;
import io.rainfall.statistics.Result;
import io.rainfall.statistics.StatisticsObserversHolder;
import io.rainfall.statistics.Task;

import java.util.List;
import java.util.Map;

import static io.rainfall.ehcache.statistics.EhcacheResult.EXCEPTION;
import static io.rainfall.ehcache.statistics.EhcacheResult.PUT;

/**
 * @author Aurelien Broszniowski
 */
public class PutOperation<K, V> extends Operation {

  @Override
  public void exec(final StatisticsObserversHolder statisticsObserversHolder, final Map<Class<? extends Configuration>,
      Configuration> configurations, final List<AssertionEvaluator> assertions) throws TestException {

    CacheConfig<K, V> cacheConfig = (CacheConfig<K, V>)configurations.get(CacheConfig.class);
    SequenceGenerator sequenceGenerator = cacheConfig.getSequenceGenerator();
    final long next = sequenceGenerator.next();
    Double weight = cacheConfig.getRandomizer().nextDouble(next);
    if (cacheConfig.getOperationWeights().get(weight) == OperationWeight.OPERATION.PUT) {
      List<Ehcache> caches = cacheConfig.getCaches();
      final ObjectGenerator<K> keyGenerator = cacheConfig.getKeyGenerator();
      final ObjectGenerator<V> valueGenerator = cacheConfig.getValueGenerator();
      for (final Ehcache cache : caches) {
        statisticsObserversHolder
            .measure(cache.getName(), EhcacheResult.values(), new Task() {

              @Override
              public Result definition() throws Exception {
                try {
                  cache.put(new Element(keyGenerator.generate(next), valueGenerator.generate(next)));
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
