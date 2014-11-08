package io.rainfall.ehcache;

import io.rainfall.Runner;
import io.rainfall.configuration.ConcurrencyConfig;
import io.rainfall.ehcache.operation.OperationWeight;
import io.rainfall.ehcache3.CacheConfig;
import io.rainfall.ehcache3.Ehcache3Operations;
import io.rainfall.generator.ByteArrayGenerator;
import io.rainfall.generator.StringGenerator;
import org.ehcache.Cache;
import org.ehcache.CacheManager;
import org.ehcache.config.CacheConfigurationBuilder;
import io.rainfall.Scenario;
import io.rainfall.SyntaxException;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import io.rainfall.configuration.ReportingConfig;

import static java.util.concurrent.TimeUnit.MINUTES;
import static org.ehcache.CacheManagerBuilder.newCacheManagerBuilder;
import static io.rainfall.execution.Executions.nothingFor;
import static io.rainfall.execution.Executions.times;
import static io.rainfall.unit.TimeDivision.seconds;

/**
 * @author Aurelien Broszniowski
 */
public class Ehcache3Test {

  private Cache<String, byte[]> cache = null;
  private CacheManager cacheManager = null;

  @Before
  public void setUp() {
    final CacheManager cacheManager = newCacheManagerBuilder()
        .withCache("one", CacheConfigurationBuilder.newCacheConfigurationBuilder()
            .buildCacheConfig(String.class, byte[].class))
        .build();

    cache = cacheManager.getCache("one", String.class, byte[].class);

    if (cache == null) {
      throw new AssertionError("Cache couldn't be initialized");
    }
  }

  @After
  public void tearDown() {
    if (cacheManager != null) {
      cacheManager.close();
    }
  }

  @Test
  public void test3() throws SyntaxException {
    CacheConfig<String, byte[]> cacheConfig = CacheConfig.<String, byte[]>cacheConfig()
        .caches(cache)
        .using(StringGenerator.fixedLength(10), ByteArrayGenerator.fixedLength(128))
        .sequentially()
        .weights(OperationWeight.operation(OperationWeight.OPERATION.PUT, 0.10), OperationWeight.operation(OperationWeight.OPERATION.GET, 0.80), OperationWeight
            .operation(OperationWeight.OPERATION.REMOVE, 0.10));
    ConcurrencyConfig concurrency = ConcurrencyConfig.concurrencyConfig()
        .threads(4).timeout(5, MINUTES);
    ReportingConfig reporting = ReportingConfig.reportingConfig(ReportingConfig.text());

    Scenario scenario = Scenario.scenario("Cache load")
        .exec(Ehcache3Operations.put())
        .exec(Ehcache3Operations.get())
        .exec(Ehcache3Operations.remove());

    Runner.setUp(scenario)
        .executed(times(1000), nothingFor(10, seconds))
        .config(cacheConfig, concurrency, reporting)
//          .assertion(latencyTime(), isLessThan(1, seconds))
        .start();
  }
}
