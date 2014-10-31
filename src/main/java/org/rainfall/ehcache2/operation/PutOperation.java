package org.rainfall.ehcache2.operation;

import net.sf.ehcache.Ehcache;
import net.sf.ehcache.Element;
import org.rainfall.AssertionEvaluator;
import org.rainfall.Configuration;
import org.rainfall.ObjectGenerator;
import org.rainfall.Operation;
import org.rainfall.SequenceGenerator;
import org.rainfall.TestException;
import org.rainfall.ehcache2.CacheConfig;
import org.rainfall.ehcache.operation.OperationWeight;
import org.rainfall.ehcache.statistics.EhcacheResult;
import org.rainfall.statistics.Result;
import org.rainfall.statistics.StatisticsObserversFactory;
import org.rainfall.statistics.Task;

import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

import static org.rainfall.ehcache.statistics.EhcacheResult.EXCEPTION;
import static org.rainfall.ehcache.statistics.EhcacheResult.PUT;

/**
 * @author Aurelien Broszniowski
 */
public class PutOperation<K, V> extends Operation {

  @Override
  public void exec(final StatisticsObserversFactory statisticsObserversFactory, final Map<Class<? extends Configuration>,
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
        statisticsObserversFactory
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
