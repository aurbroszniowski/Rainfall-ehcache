package org.rainfall.ehcache;

import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Ehcache;
import net.sf.ehcache.config.CacheConfiguration;
import net.sf.ehcache.config.Configuration;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.rainfall.Runner;
import org.rainfall.Scenario;
import org.rainfall.SyntaxException;
import org.rainfall.configuration.ConcurrencyConfig;
import org.rainfall.configuration.ReportingConfig;
import org.rainfall.generator.ByteArrayGenerator;
import org.rainfall.generator.StringGenerator;
import org.rainfall.generator.sequence.Distribution;
import org.rainfall.utils.SystemTest;

import static java.util.concurrent.TimeUnit.MINUTES;
import static org.rainfall.ehcache.EhcacheOperations.get;
import static org.rainfall.ehcache.EhcacheOperations.put;
import static org.rainfall.ehcache.EhcacheOperations.remove;
import static org.rainfall.ehcache.operation.OperationWeight.OPERATION.GET;
import static org.rainfall.ehcache.operation.OperationWeight.OPERATION.PUT;
import static org.rainfall.ehcache.operation.OperationWeight.OPERATION.REMOVE;
import static org.rainfall.ehcache.operation.OperationWeight.operation;
import static org.rainfall.execution.Executions.during;
import static org.rainfall.execution.Executions.nothingFor;
import static org.rainfall.execution.Executions.times;
import static org.rainfall.unit.TimeDivision.minutes;
import static org.rainfall.unit.TimeDivision.seconds;

/**
 * @author Aurelien Broszniowski
 */

@Category(SystemTest.class)
public class EhcacheTest {

  private Ehcache cache = null;
  private CacheManager cacheManager = null;

  @Before
  public void setUp() {
    Configuration configuration = new Configuration().name("EhcacheTest")
        .defaultCache(new CacheConfiguration("default", 0))
        .cache(new CacheConfiguration("one", 0));
    cacheManager = CacheManager.create(configuration);
    cache = cacheManager.getEhcache("one");
    if (cache == null) {
      throw new AssertionError("Cache couldn't be initialized");
    }
  }

  @After
  public void tearDown() {
    if (cacheManager != null) {
      cacheManager.shutdown();
    }
  }

  @Test
  public void testLoad() throws SyntaxException {
    CacheConfig<String, Byte[]> cacheConfig = CacheConfig.<String, Byte[]>cacheConfig()
        .caches(cache)
        .using(StringGenerator.fixedLength(10), ByteArrayGenerator.fixedLength(128))
        .sequentially()
        .weights(operation(PUT, 0.10), operation(GET, 0.80), operation(REMOVE, 0.10));
    ConcurrencyConfig concurrency = ConcurrencyConfig.concurrencyConfig()
        .threads(4).timeout(5, MINUTES);
    ReportingConfig reporting = ReportingConfig.reportingConfig(ReportingConfig.text(), ReportingConfig.html());

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

  @Test
  public void testLength() throws SyntaxException {
    CacheConfig<String, Byte[]> cacheConfig = CacheConfig.<String, Byte[]>cacheConfig()
        .caches(cache)
        .using(StringGenerator.fixedLength(10), ByteArrayGenerator.fixedLength(128))
        .sequentially()
        .weights(operation(PUT, 0.10), operation(GET, 0.80), operation(REMOVE, 0.10));

    ConcurrencyConfig concurrency = ConcurrencyConfig.concurrencyConfig()
        .threads(4).timeout(5, MINUTES);
    ReportingConfig reporting = ReportingConfig.reportingConfig(ReportingConfig.text(), ReportingConfig.html());

    Scenario scenario = Scenario.scenario("Cache load")
        .exec(put())
        .exec(get())
        .exec(remove());

    Runner.setUp(scenario)
        .executed(during(25, seconds))
        .config(cacheConfig, concurrency, reporting)
        .start();
  }

  @Test
  public void testRandomAccess() throws SyntaxException {
    CacheConfig<String, Byte[]> cacheConfig = CacheConfig.<String, Byte[]>cacheConfig()
        .caches(cache)
        .using(StringGenerator.fixedLength(10), ByteArrayGenerator.fixedLength(128))
        .atRandom(Distribution.GAUSSIAN, 0, 10000, 1000)
        .weights(operation(PUT, 0.10), operation(GET, 0.80), operation(REMOVE, 0.10));

    ConcurrencyConfig concurrency = ConcurrencyConfig.concurrencyConfig()
        .threads(4).timeout(5, MINUTES);
    ReportingConfig reporting = ReportingConfig.reportingConfig(ReportingConfig.text(), ReportingConfig.html());

    Scenario scenario = Scenario.scenario("Cache load")
        .exec(put())
        .exec(get())
        .exec(remove());

    Runner.setUp(scenario)
        .executed(during(25, seconds))
        .config(cacheConfig, concurrency, reporting)
        .start();
  }
}
