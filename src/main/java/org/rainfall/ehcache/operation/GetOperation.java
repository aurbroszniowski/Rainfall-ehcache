package org.rainfall.ehcache.operation;

import net.sf.ehcache.Ehcache;
import net.sf.ehcache.Element;
import org.rainfall.AssertionEvaluator;
import org.rainfall.Configuration;
import org.rainfall.ObjectGenerator;
import org.rainfall.Operation;
import org.rainfall.SequenceGenerator;
import org.rainfall.TestException;
import org.rainfall.ehcache.CacheConfig;
import org.rainfall.ehcache.statistics.EhcacheResult;
import org.rainfall.statistics.Result;
import org.rainfall.statistics.StatisticsObserversFactory;
import org.rainfall.statistics.Task;

import java.util.List;
import java.util.Map;

import static org.rainfall.ehcache.statistics.EhcacheResult.EXCEPTION;
import static org.rainfall.ehcache.statistics.EhcacheResult.GET;
import static org.rainfall.ehcache.statistics.EhcacheResult.MISS;

/**
 * @author Aurelien Broszniowski
 */
public class GetOperation<K, V> extends Operation {

  @Override
  public void exec(final StatisticsObserversFactory statisticsObserversFactory, final Map<Class<? extends Configuration>,
      Configuration> configurations, final List<AssertionEvaluator> assertions) throws TestException {

    CacheConfig<K, V> cacheConfig = (CacheConfig<K, V>)configurations.get(CacheConfig.class);
    SequenceGenerator sequenceGenerator = cacheConfig.getSequenceGenerator();
    final long next = sequenceGenerator.next();
    Double weight = cacheConfig.getRandomizer().nextDouble(next);
    if (cacheConfig.getOperationWeights().get(weight) == OperationWeight.OPERATION.GET) {
      List<Ehcache> caches = cacheConfig.getCaches();
      final ObjectGenerator<K> keyGenerator = cacheConfig.getKeyGenerator();
      for (final Ehcache cache : caches) {
        statisticsObserversFactory.getStatisticObserver(cache.getName(), EhcacheResult.values())
            .measure(new Task() {

              @Override
              public Result definition() throws Exception {
                Element value;
                try {
                  value = cache.get(keyGenerator.generate(next));
                } catch (Exception e) {
                  return EXCEPTION;
                }
                if (value == null) {
                  return MISS;
                } else {
                  return GET;
                }
              }
            });
      }
    }
  }
}
