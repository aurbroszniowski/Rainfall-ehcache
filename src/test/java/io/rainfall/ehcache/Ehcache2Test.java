package io.rainfall.ehcache;

import io.rainfall.Runner;
import io.rainfall.Scenario;
import io.rainfall.SyntaxException;
import io.rainfall.configuration.ConcurrencyConfig;
import io.rainfall.configuration.ReportingConfig;
import io.rainfall.ehcache.statistics.EhcacheResult;
import io.rainfall.ehcache2.CacheConfig;
import io.rainfall.generator.ByteArrayGenerator;
import io.rainfall.generator.StringGenerator;
import io.rainfall.generator.sequence.Distribution;
import io.rainfall.utils.SystemTest;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Ehcache;
import net.sf.ehcache.config.CacheConfiguration;
import net.sf.ehcache.config.Configuration;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import static io.rainfall.configuration.ReportingConfig.html;
import static io.rainfall.configuration.ReportingConfig.reportingConfig;
import static io.rainfall.configuration.ReportingConfig.text;
import static io.rainfall.ehcache2.Ehcache2Operations.get;
import static io.rainfall.ehcache2.Ehcache2Operations.put;
import static io.rainfall.ehcache2.Ehcache2Operations.remove;
import static io.rainfall.execution.Executions.during;
import static io.rainfall.execution.Executions.times;
import static io.rainfall.unit.TimeDivision.minutes;
import static io.rainfall.unit.TimeDivision.seconds;
import static java.util.concurrent.TimeUnit.MINUTES;

/**
 * @author Aurelien Broszniowski
 */

@Category(SystemTest.class)
public class Ehcache2Test {

  private Ehcache cache1 = null;
  private Ehcache cache2 = null;
  private Ehcache cache3 = null;
  private CacheManager cacheManager = null;

  @Before
  public void setUp() {
    Configuration configuration = new Configuration().name("EhcacheTest")
        .defaultCache(new CacheConfiguration("default", 0))
        .cache(new CacheConfiguration("one", 0))
        .cache(new CacheConfiguration("two", 0))
        .cache(new CacheConfiguration("three", 0));
    cacheManager = CacheManager.create(configuration);
    cache1 = cacheManager.getEhcache("one");
    cache2 = cacheManager.getEhcache("two");
    cache3 = cacheManager.getEhcache("three");
    if (cache1 == null || cache2 == null || cache3 == null) {
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
    CacheConfig<String, byte[]> cacheConfig = CacheConfig.<String, byte[]>cacheConfig()
        .caches(cache1, cache2, cache3)
        .using(StringGenerator.fixedLength(10), ByteArrayGenerator.fixedLength(128))
        .sequentially();
    ConcurrencyConfig concurrency = ConcurrencyConfig.concurrencyConfig()
        .threads(4).timeout(5, MINUTES);
    ReportingConfig reporting = reportingConfig(EhcacheResult.class, text(), html());

    Scenario scenario = Scenario.scenario("Cache load")
        .exec(put());

    Runner.setUp(scenario)
        .executed(times(1000))
        .config(cacheConfig, concurrency, reporting)
        .start();

    System.out.println(cache1.getSize());
    System.out.println(cache2.getSize());
    System.out.println(cache3.getSize());
  }

  @Test
  public void testLength() throws SyntaxException {
    CacheConfig<String, byte[]> cacheConfig = CacheConfig.<String, byte[]>cacheConfig()
        .caches(cache1, cache2, cache3)
        .using(StringGenerator.fixedLength(10), ByteArrayGenerator.fixedLength(128))
        .sequentially();

    ConcurrencyConfig concurrency = ConcurrencyConfig.concurrencyConfig()
        .threads(4).timeout(5, MINUTES);
    ReportingConfig<EhcacheResult> reporting = reportingConfig(EhcacheResult.class, text(), html());

    Scenario scenario = Scenario.scenario("Cache load")
        .exec(put().withWeight(0.10))
        .exec(get().withWeight(0.80))
        .exec(remove().withWeight(0.10));

    Runner.setUp(scenario)
        .executed(during(20, seconds))
        .config(cacheConfig, concurrency, reporting)
        .start();
  }

  @Test
  public void testRandomAccess() throws SyntaxException {
    CacheConfig<String, byte[]> cacheConfig = CacheConfig.<String, byte[]>cacheConfig()
        .caches(cache1, cache2, cache3)
        .using(StringGenerator.fixedLength(10), ByteArrayGenerator.fixedLength(128))
        .atRandom(Distribution.GAUSSIAN, 0, 10000, 1000);

    ConcurrencyConfig concurrency = ConcurrencyConfig.concurrencyConfig()
        .threads(4).timeout(5, MINUTES);
    ReportingConfig reporting = reportingConfig(EhcacheResult.class, text(), html());

    Scenario scenario = Scenario.scenario("Cache load")
        .exec(put().withWeight(0.10))
        .exec(get().withWeight(0.80))
        .exec(remove().withWeight(0.10));

    Runner.setUp(scenario)
        .executed(during(10, seconds))
        .config(cacheConfig, concurrency, reporting)
        .start();
  }

  @Test
  public void testRemove() throws SyntaxException {
    CacheConfig<String, byte[]> cacheConfig = CacheConfig.<String, byte[]>cacheConfig()
        .caches(cache1, cache2, cache3)
        .using(StringGenerator.fixedLength(10), ByteArrayGenerator.fixedLength(128))
        .sequentially();

    ConcurrencyConfig concurrency = ConcurrencyConfig.concurrencyConfig()
        .threads(4).timeout(5, MINUTES);
    ReportingConfig reporting = reportingConfig(EhcacheResult.class, text(), html());

    Scenario scenario = Scenario.scenario("Cache load")
        .exec(put().withWeight(0.10))
        .exec(get().withWeight(0.80))
        .exec(remove().withWeight(0.10));

    Runner.setUp(scenario)
        .executed(during(10, seconds))
        .config(cacheConfig, concurrency, reporting)
        .start();
  }

}
