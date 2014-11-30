package io.rainfall.ehcache;

import io.rainfall.ObjectGenerator;
import io.rainfall.Runner;
import io.rainfall.Scenario;
import io.rainfall.SyntaxException;
import io.rainfall.configuration.ConcurrencyConfig;
import io.rainfall.ehcache.statistics.EhcacheResult;
import io.rainfall.ehcache3.CacheConfig;
import io.rainfall.generator.ByteArrayGenerator;
import io.rainfall.generator.StringGenerator;
import io.rainfall.generator.sequence.Distribution;
import org.ehcache.Cache;
import org.ehcache.CacheManager;
import org.ehcache.config.CacheConfigurationBuilder;
import org.junit.Ignore;
import org.junit.Test;

import static io.rainfall.configuration.ReportingConfig.html;
import static io.rainfall.configuration.ReportingConfig.reportingConfig;
import static io.rainfall.configuration.ReportingConfig.text;
import static io.rainfall.ehcache3.Ehcache3Operations.get;
import static io.rainfall.ehcache3.Ehcache3Operations.put;
import static io.rainfall.execution.Executions.during;
import static io.rainfall.execution.Executions.times;
import static io.rainfall.unit.TimeDivision.minutes;
import static java.util.concurrent.TimeUnit.MINUTES;
import static org.ehcache.CacheManagerBuilder.newCacheManagerBuilder;

/**
 * @author Aurelien Broszniowski
 */
public class PerfTest3 {

  @Test
  @Ignore
  public void testLoad() throws SyntaxException {
    CacheConfigurationBuilder<Object, Object> builder = CacheConfigurationBuilder.newCacheConfigurationBuilder();
    builder.maxEntriesInCache(250000L);

    final CacheManager cacheManager = newCacheManagerBuilder()
        .withCache("one", builder.buildConfig(String.class, byte[].class))
        .withCache("two", builder.buildConfig(String.class, byte[].class))
        .withCache("three", builder.buildConfig(String.class, byte[].class))
        .withCache("four", builder.buildConfig(String.class, byte[].class))
        .build();

    final Cache<String, byte[]> one = cacheManager.getCache("one", String.class, byte[].class);
    final Cache<String, byte[]> two = cacheManager.getCache("two", String.class, byte[].class);
    final Cache<String, byte[]> three = cacheManager.getCache("three", String.class, byte[].class);
    final Cache<String, byte[]> four = cacheManager.getCache("four", String.class, byte[].class);

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

    System.out.println("----------> Test phase");

    Runner.setUp(
        Scenario.scenario("Test phase").exec(put().withWeight(0.10)).exec(get().withWeight(0.90)))
        .executed(during(5, minutes))
        .config(concurrency, reportingConfig(EhcacheResult.class, text(), html()))
        .config(CacheConfig.<String, byte[]>cacheConfig()
            .caches(one, two, three, four)
            .using(keyGenerator, valueGenerator)
            .atRandom(Distribution.GAUSSIAN, 0, nbElements, 10000))
        .start();

    System.out.println("----------> Done");

    cacheManager.close();
  }
}
