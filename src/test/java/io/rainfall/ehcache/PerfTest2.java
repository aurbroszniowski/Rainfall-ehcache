package io.rainfall.ehcache;

import io.rainfall.ObjectGenerator;
import io.rainfall.RainfallMaster;
import io.rainfall.Runner;
import io.rainfall.Scenario;
import io.rainfall.SyntaxException;
import io.rainfall.TestException;
import io.rainfall.configuration.ConcurrencyConfig;
import io.rainfall.configuration.DistributedConfig;
import io.rainfall.configuration.ReportingConfig;
import io.rainfall.ehcache.statistics.EhcacheResult;
import io.rainfall.ehcache2.CacheConfig;
import io.rainfall.ehcache2.execution.UntilCacheFull;
import io.rainfall.generator.ByteArrayGenerator;
import io.rainfall.generator.DistributedLongSequenceGenerator;
import io.rainfall.generator.StringGenerator;
import io.rainfall.generator.sequence.Distribution;
import io.rainfall.statistics.StatisticsPeekHolder;
import io.rainfall.utils.SystemTest;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Ehcache;
import net.sf.ehcache.config.CacheConfiguration;
import net.sf.ehcache.config.Configuration;
import net.sf.ehcache.config.MemoryUnit;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.io.File;
import java.util.UUID;

import static io.rainfall.Scenario.weighted;
import static io.rainfall.configuration.DistributedConfig.address;
import static io.rainfall.configuration.ReportingConfig.hlog;
import static io.rainfall.configuration.ReportingConfig.html;
import static io.rainfall.configuration.ReportingConfig.text;
import static io.rainfall.ehcache2.CacheDefinition.cache;
import static io.rainfall.ehcache2.Ehcache2Operations.get;
import static io.rainfall.ehcache2.Ehcache2Operations.put;
import static io.rainfall.execution.Executions.during;
import static io.rainfall.execution.Executions.times;
import static io.rainfall.generator.SequencesGenerator.atRandom;
import static io.rainfall.generator.SequencesGenerator.sequentially;
import static io.rainfall.unit.TimeDivision.minutes;
import static io.rainfall.unit.TimeDivision.seconds;
import static java.util.concurrent.TimeUnit.MINUTES;

/**
 * @author Aurelien Broszniowski
 */
@Category(SystemTest.class)
public class PerfTest2 {

  @Test
  @Ignore
  public void testDistributedLoad() throws SyntaxException, TestException {
    DistributedConfig distributedConfig = DistributedConfig.distributedConfig(address("localhost", 9911), 2);
    RainfallMaster rainfallMaster = null;
    CacheManager cacheManager = null;
    try {
      rainfallMaster = RainfallMaster.master(distributedConfig, new File("rainfall-dist")).start();

      Configuration configuration = new Configuration().name("EhcacheTest")
          .defaultCache(new CacheConfiguration("default", 0).eternal(true))
          .cache(new CacheConfiguration().name("one")
              .maxBytesLocalHeap(100, MemoryUnit.MEGABYTES));
      cacheManager = CacheManager.create(configuration);

      Ehcache one = cacheManager.getEhcache("one");

      ConcurrencyConfig concurrency = ConcurrencyConfig.concurrencyConfig()
          .threads(4).timeout(50, MINUTES);

      int nbElements = 250000;
      ObjectGenerator<String> keyGenerator = StringGenerator.fixedLengthString(10);
      ObjectGenerator<byte[]> valueGenerator = ByteArrayGenerator.fixedLengthByteArray(1000);

      Runner.setUp(
          Scenario.scenario("warmup phase").exec(
              put(keyGenerator, valueGenerator, new DistributedLongSequenceGenerator(distributedConfig), cache("one", one))
          ))
          .executed(times(nbElements))
          .config(distributedConfig)
          .config(concurrency, ReportingConfig.report(EhcacheResult.class).log(text(),
              html("rainfall-distributed-" + UUID.randomUUID().toString())))
          .config(CacheConfig.<String, byte[]>cacheConfig().caches(one))
//          .start()
          ;

      StatisticsPeekHolder finalStats = Runner.setUp(
          Scenario.scenario("Test phase").exec(
              weighted(0.20,
                  put(keyGenerator, valueGenerator,
                      atRandom(Distribution.GAUSSIAN, 0, nbElements, 10000), cache("one", one)))
              ,
              weighted(0.80, get(String.class, byte[].class)
                  .atRandom(Distribution.GAUSSIAN, 0, nbElements, 10000)
                  .using(keyGenerator, valueGenerator))
          ))
          .executed(during(20, seconds))
          .config(concurrency, ReportingConfig.report(EhcacheResult.class).log(text(),
              hlog("rainfall-distributed-" + UUID.randomUUID().toString(), true)))
          .config(CacheConfig.<String, byte[]>cacheConfig().caches(one))
          .config(distributedConfig)
          .start();

    } finally {
      if (cacheManager != null) {
        cacheManager.shutdown();
      }
      if (rainfallMaster != null) {
        rainfallMaster.stop();
      }
    }
  }

  @Test
  @Ignore
  public void testLoad() throws SyntaxException {

    CacheManager cacheManager = null;
    try {
      Configuration configuration = new Configuration().name("EhcacheTest")
          .defaultCache(new CacheConfiguration("default", 0).eternal(true))
          .cache(new CacheConfiguration().name("one")
                  .maxBytesLocalHeap(100, MemoryUnit.MEGABYTES)
//              .maxBytesLocalOffHeap(200, MemoryUnit.MEGABYTES)
          )
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
      ObjectGenerator<String> keyGenerator = StringGenerator.fixedLengthString(10);
      ObjectGenerator<byte[]> valueGenerator = ByteArrayGenerator.fixedLengthByteArray(1000);

      System.out.println("----------> Warm up phase");

      Runner.setUp(
          Scenario.scenario("Warm up phase")
              .exec(
                  put(keyGenerator, valueGenerator, sequentially(), cache("one", one), cache("two", two)),
                  put(keyGenerator, valueGenerator, sequentially(), cache("three", three), cache("four", four))
              ))
          .executed(new UntilCacheFull())
          .config(concurrency, ReportingConfig.report(EhcacheResult.class, new EhcacheResult[] { EhcacheResult.PUT })
              .log(text()))
          .config(CacheConfig.<String, byte[]>cacheConfig()
              .caches(one, two, three, four)
          )
          .start();

      try {
        Thread.sleep(1000);
      } catch (InterruptedException e) {
        e.printStackTrace();
      }

      System.out.println(one.getStatistics().getLocalHeapSize());
      System.out.println(two.getStatistics().getLocalHeapSize());
      System.out.println(three.getStatistics().getLocalHeapSize());
      System.out.println(four.getStatistics().getLocalHeapSize());

      System.out.println("----------> Test phase");

      StatisticsPeekHolder finalStats = Runner.setUp(
          Scenario.scenario("Test phase").exec(
              weighted(0.90, put(keyGenerator, valueGenerator,
                  atRandom(Distribution.GAUSSIAN, 0, nbElements, 10000),
                  cache("three", three), cache("four", four))),
              weighted(0.10, get(String.class, byte[].class)
                  .atRandom(Distribution.GAUSSIAN, 0, nbElements, 10000)
                  .using(keyGenerator, valueGenerator))
          ))
          .executed(during(5, minutes))
          .config(concurrency, ReportingConfig.report(EhcacheResult.class).log(text(), html()))
          .config(CacheConfig.<String, byte[]>cacheConfig()
              .caches(one, two, three, four)
          )
          .start();

      System.out.println("----------> Done");

    } finally {
      if (cacheManager != null) {
        cacheManager.shutdown();
      }
    }
  }
}
