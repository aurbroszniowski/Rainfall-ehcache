/*
 * Copyright 2014 Aurélien Broszniowski
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.rainfall.ehcache;

import io.rainfall.ObjectGenerator;
import io.rainfall.Runner;
import io.rainfall.Scenario;
import io.rainfall.SyntaxException;
import io.rainfall.configuration.ConcurrencyConfig;
import io.rainfall.configuration.ReportingConfig;
import io.rainfall.ehcache.statistics.EhcacheResult;
import io.rainfall.ehcache3.CacheConfig;
import io.rainfall.generator.ByteArrayGenerator;
import io.rainfall.generator.LongGenerator;
import io.rainfall.statistics.StatisticsPeekHolder;
import org.ehcache.Cache;
import org.ehcache.CacheManager;
import org.ehcache.config.CacheConfigurationBuilder;
import org.junit.Ignore;
import org.junit.Test;

import static io.rainfall.configuration.ReportingConfig.html;
import static io.rainfall.configuration.ReportingConfig.text;
import static io.rainfall.ehcache.statistics.EhcacheResult.MISS;
import static io.rainfall.ehcache.statistics.EhcacheResult.PUT;
import static io.rainfall.ehcache.statistics.EhcacheResult.PUTALL;
import static io.rainfall.ehcache3.CacheConfig.cacheConfig;
import static io.rainfall.ehcache3.Ehcache3Operations.put;
import static io.rainfall.ehcache3.Ehcache3Operations.removeForKeyAndValue;
import static io.rainfall.ehcache3.Ehcache3Operations.replace;
import static io.rainfall.ehcache3.Ehcache3Operations.replaceForKeyAndValue;
import static io.rainfall.execution.Executions.during;
import static io.rainfall.execution.Executions.times;
import static io.rainfall.generator.sequence.Distribution.GAUSSIAN;
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
        .withCache("one", builder.buildConfig(Long.class, byte[].class))
        .withCache("two", builder.buildConfig(Long.class, byte[].class))
        .withCache("three", builder.buildConfig(Long.class, byte[].class))
        .withCache("four", builder.buildConfig(Long.class, byte[].class))
        .build();

    final Cache<Long, byte[]> one = cacheManager.getCache("one", Long.class, byte[].class);
    final Cache<Long, byte[]> two = cacheManager.getCache("two", Long.class, byte[].class);
    final Cache<Long, byte[]> three = cacheManager.getCache("three", Long.class, byte[].class);
    final Cache<Long, byte[]> four = cacheManager.getCache("four", Long.class, byte[].class);

    ConcurrencyConfig concurrency = ConcurrencyConfig.concurrencyConfig()
        .threads(4).timeout(50, MINUTES);

    int nbElements = 250000;
    ObjectGenerator<Long> keyGenerator = new LongGenerator();
    ObjectGenerator<byte[]> valueGenerator = ByteArrayGenerator.fixedLength(1000);

    EhcacheResult[] resultsReported = new EhcacheResult[] { PUT, PUTALL, MISS };

    System.out.println("----------> Warm up phase");
    Runner.setUp(
        Scenario.scenario("Warm up phase").exec(
            put(Long.class, byte[].class).using(keyGenerator, valueGenerator).sequentially()
        ))
        .executed(times(nbElements))
        .config(concurrency, ReportingConfig.report(EhcacheResult.class, resultsReported).log(text()))
        .config(cacheConfig(Long.class, byte[].class)
                .caches(one, two, three, four).bulkBatchSize(5)
        )
        .start()
    ;

    try {
      Thread.sleep(1000);
    } catch (InterruptedException e) {
      e.printStackTrace();
    }

    System.out.println(one.getStatistics().getCachePuts());
    System.out.println(two.getStatistics().getCachePuts());
    System.out.println(three.getStatistics().getCachePuts());
    System.out.println(four.getStatistics().getCachePuts());

    System.out.println("----------> Test phase");

    StatisticsPeekHolder finalStats = Runner.setUp(
        Scenario.scenario("Test phase").exec(
//            put(Long.class, byte[].class).withWeight(0.10)
//                .using(keyGenerator, valueGenerator)
//                .atRandom(GAUSSIAN, 0, nbElements, 10000),
//            get(Long.class, byte[].class).withWeight(0.80)
//                .using(keyGenerator, valueGenerator)
//                .atRandom(GAUSSIAN, 0, nbElements, 10000),
//            putAll(Long.class, byte[].class).withWeight(0.10)
//                .using(keyGenerator, valueGenerator)
//                .atRandom(GAUSSIAN, 0, nbElements, 10000),
//            getAll(Long.class, byte[].class).withWeight(0.40)
//                .using(keyGenerator, valueGenerator)
//                .atRandom(GAUSSIAN, 0, nbElements, 10000),
//            removeAll(Long.class, byte[].class).withWeight(0.10)
//                .using(keyGenerator, valueGenerator)
//                .atRandom(GAUSSIAN, 0, nbElements, 10000),
//            putIfAbsent(Long.class, byte[].class).withWeight(0.10)
//                .using(keyGenerator, valueGenerator)
//                .atRandom(GAUSSIAN, 0, nbElements, 10000),
//            replace(Long.class, byte[].class).withWeight(0.10)
//                .using(keyGenerator, valueGenerator)
//                .atRandom(GAUSSIAN, 0, nbElements, 10000)
            replaceForKeyAndValue(Long.class, byte[].class).withWeight(0.10)
                .using(keyGenerator, valueGenerator)
                .atRandom(GAUSSIAN, 0, nbElements, 10000)
        ))
        .executed(during(1, minutes))
        .config(concurrency, ReportingConfig.report(EhcacheResult.class).log(text(), html()).summary(text()))
        .config(cacheConfig(Long.class, byte[].class)
            .caches(one, two, three, four).bulkBatchSize(10))
        .start();

    System.out.println("----------> Done");

    cacheManager.close();
  }

  @Test
  public void testReplace() throws SyntaxException {
    CacheConfigurationBuilder<Object, Object> builder = CacheConfigurationBuilder.newCacheConfigurationBuilder();
    builder.maxEntriesInCache(250000L);

    final CacheManager cacheManager = newCacheManagerBuilder()
        .withCache("one", builder.buildConfig(Long.class, Long.class))
        .build();

    final Cache<Long, Long> one = cacheManager.getCache("one", Long.class, Long.class);

    ConcurrencyConfig concurrency = ConcurrencyConfig.concurrencyConfig()
        .threads(4).timeout(50, MINUTES);

    int nbElements = 250000;
    ObjectGenerator<Long> keyGenerator = new LongGenerator();
    ObjectGenerator<Long> valueGenerator = new LongGenerator();

    ReportingConfig reportingConfig = ReportingConfig.report(EhcacheResult.class).log(text());
    CacheConfig<Long, Long> cacheConfig = cacheConfig(Long.class, Long.class).caches(one);
    Runner.setUp(
        Scenario.scenario("warmup phase").exec(
            put(Long.class, Long.class).using(keyGenerator, valueGenerator).sequentially()
        ))
        .executed(times(nbElements))
        .config(concurrency, reportingConfig)
        .config(cacheConfig)
        .start()
    ;
    Runner.setUp(
        Scenario.scenario("Test phase").exec(
            removeForKeyAndValue(Long.class, Long.class).using(keyGenerator, valueGenerator).sequentially()
        ))
        .executed(during(1, minutes))
        .config(concurrency, reportingConfig)
        .config(cacheConfig)
        .start()
    ;
    cacheManager.close();
  }
}
