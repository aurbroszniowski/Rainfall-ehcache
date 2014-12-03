package io.rainfall.ehcache;

import io.rainfall.ObjectGenerator;
import io.rainfall.Runner;
import io.rainfall.Scenario;
import io.rainfall.SyntaxException;
import io.rainfall.configuration.ConcurrencyConfig;
import io.rainfall.ehcache.statistics.EhcacheResult;
import io.rainfall.ehcache2.CacheConfig;
import io.rainfall.generator.ByteArrayGenerator;
import io.rainfall.generator.StringGenerator;
import io.rainfall.generator.sequence.Distribution;
import io.rainfall.statistics.StatisticsPeekHolder;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Ehcache;
import net.sf.ehcache.config.CacheConfiguration;
import net.sf.ehcache.config.Configuration;
import org.junit.Ignore;
import org.junit.Test;

import static io.rainfall.configuration.ReportingConfig.html;
import static io.rainfall.configuration.ReportingConfig.reportingConfig;
import static io.rainfall.configuration.ReportingConfig.text;
import static io.rainfall.ehcache2.Ehcache2Operations.get;
import static io.rainfall.ehcache2.Ehcache2Operations.put;
import static io.rainfall.execution.Executions.during;
import static io.rainfall.execution.Executions.times;
import static io.rainfall.unit.TimeDivision.minutes;
import static java.util.concurrent.TimeUnit.MINUTES;

/**
 * @author Aurelien Broszniowski
 */
public class PerfTest2 {

  @Test
  @Ignore
  public void testLoad() throws SyntaxException {

    CacheManager cacheManager = null;
    try {
      Configuration configuration = new Configuration().name("EhcacheTest")
          .defaultCache(new CacheConfiguration("default", 0))
          .cache(new CacheConfiguration("one", 250000))
          .cache(new CacheConfiguration("two", 250000))
          .cache(new CacheConfiguration("three", 250000))
          .cache(new CacheConfiguration("four", 250000));
      cacheManager = CacheManager.create(configuration);

      Ehcache one = cacheManager.getEhcache("one");
      Ehcache two = cacheManager.getEhcache("two");
      Ehcache three = cacheManager.getEhcache("three");
      Ehcache four = cacheManager.getEhcache("four");

      ConcurrencyConfig concurrency = ConcurrencyConfig.concurrencyConfig()
          .threads(4).timeout(50, MINUTES);

      int nbElements = 250000;
      ObjectGenerator<String> keyGenerator = StringGenerator.fixedLength(10);
      ObjectGenerator<byte[]> valueGenerator = ByteArrayGenerator.fixedLength(1000);

      System.out.println("----------> Warm up phase");
      Runner.setUp(
          Scenario.scenario("Warm up phase").exec(put()))
          .executed(times(nbElements))
          .config(concurrency, reportingConfig(EhcacheResult.class, text()))
          .config(CacheConfig.<String, byte[]>cacheConfig()
                  .caches(one, two, three, four)
                  .using(keyGenerator, valueGenerator)
                  .sequentially()
          )
          .start();

      System.out.println(one.getStatistics().getLocalHeapSize());
      System.out.println(two.getStatistics().getLocalHeapSize());
      System.out.println(three.getStatistics().getLocalHeapSize());
      System.out.println(four.getStatistics().getLocalHeapSize());

      System.out.println("----------> Test phase");

      StatisticsPeekHolder finalStats = Runner.setUp(
          Scenario.scenario("Test phase").exec(put().withWeight(0.90)).exec(get().withWeight(0.10)))
          .executed(during(5, minutes))
          .config(concurrency, reportingConfig(EhcacheResult.class, text(), html()))
          .config(CacheConfig.<String, byte[]>cacheConfig()
              .caches(one, two, three, four)
              .using(keyGenerator, valueGenerator)
              .atRandom(Distribution.GAUSSIAN, 0, nbElements, 10000))
          .start();

      System.out.println("----------> Done");

    } finally {
      if (cacheManager != null) {
        cacheManager.shutdown();
      }
    }
  }
}
