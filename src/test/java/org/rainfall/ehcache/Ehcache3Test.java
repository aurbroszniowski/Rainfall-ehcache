package org.rainfall.ehcache;

import org.ehcache.Cache;
import org.ehcache.CacheManager;
import org.ehcache.config.CacheConfigurationBuilder;
import org.rainfall.ObjectGenerator;
import org.rainfall.Runner;
import org.rainfall.Scenario;
import org.rainfall.SyntaxException;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.rainfall.Runner;
import org.rainfall.Scenario;
import org.rainfall.SyntaxException;
import org.rainfall.configuration.ConcurrencyConfig;
import org.rainfall.configuration.ReportingConfig;
import org.rainfall.ehcache3.CacheConfig;
import org.rainfall.generator.ByteArrayGenerator;
import org.rainfall.generator.StringGenerator;
import org.rainfall.generator.sequence.Distribution;
import org.rainfall.utils.SystemTest;

import static java.util.concurrent.TimeUnit.MINUTES;
import static org.ehcache.CacheManagerBuilder.newCacheManagerBuilder;
import static org.rainfall.ehcache3.Ehcache3Operations.get;
import static org.rainfall.ehcache3.Ehcache3Operations.put;
import static org.rainfall.ehcache3.Ehcache3Operations.remove;
import static org.rainfall.ehcache.operation.OperationWeight.OPERATION.GET;
import static org.rainfall.ehcache.operation.OperationWeight.OPERATION.PUT;
import static org.rainfall.ehcache.operation.OperationWeight.OPERATION.REMOVE;
import static org.rainfall.ehcache.operation.OperationWeight.operation;
import static org.rainfall.execution.Executions.during;
import static org.rainfall.execution.Executions.nothingFor;
import static org.rainfall.execution.Executions.times;
import static org.rainfall.unit.TimeDivision.seconds;

import org.junit.Test;

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
        .weights(operation(PUT, 0.10), operation(GET, 0.80), operation(REMOVE, 0.10));
    ConcurrencyConfig concurrency = ConcurrencyConfig.concurrencyConfig()
        .threads(4).timeout(5, MINUTES);
    ReportingConfig reporting = ReportingConfig.reportingConfig(ReportingConfig.text());

    Scenario scenario = Scenario.scenario("Cache load")
        .exec(put())
        .exec(get())
        .exec(remove());

    Runner.setUp(scenario)
        .executed(times(10000000), nothingFor(10, seconds))
        .config(cacheConfig, concurrency, reporting)
//          .assertion(latencyTime(), isLessThan(1, seconds))
        .start();
  }
}
