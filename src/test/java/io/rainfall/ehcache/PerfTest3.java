/*
 * Copyright (c) 2014-2022 Aurélien Broszniowski
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
import io.rainfall.ScenarioRun;
import io.rainfall.SyntaxException;
import io.rainfall.configuration.ConcurrencyConfig;
import io.rainfall.configuration.ReportingConfig;
import io.rainfall.ehcache.statistics.EhcacheResult;
import io.rainfall.ehcache3.CacheConfig;
import io.rainfall.ehcache3.CacheDefinition;
import io.rainfall.generator.IterationSequenceGenerator;
import io.rainfall.generator.LongGenerator;
import io.rainfall.generator.VerifiedValueGenerator;
import io.rainfall.generator.VerifiedValueGenerator.VerifiedValue;
import io.rainfall.statistics.StatisticsPeekHolder;
import io.rainfall.utils.SystemTest;
import org.ehcache.Cache;
import org.ehcache.CacheManager;
import org.ehcache.config.builders.CacheConfigurationBuilder;
import org.ehcache.config.builders.CacheManagerBuilder;
import org.ehcache.config.units.EntryUnit;
import org.ehcache.config.units.MemoryUnit;
import org.ehcache.impl.config.persistence.CacheManagerPersistenceConfiguration;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.openjdk.jol.info.GraphLayout;

import java.io.File;
import java.util.Date;
import java.util.EnumSet;
import java.util.concurrent.TimeUnit;

import static io.rainfall.Scenario.scenario;
import static io.rainfall.Scenario.weighted;
import static io.rainfall.configuration.ReportingConfig.gcStatistics;
import static io.rainfall.configuration.ReportingConfig.hlog;
import static io.rainfall.configuration.ReportingConfig.html;
import static io.rainfall.configuration.ReportingConfig.report;
import static io.rainfall.configuration.ReportingConfig.text;
import static io.rainfall.ehcache.statistics.EhcacheResult.GET;
import static io.rainfall.ehcache.statistics.EhcacheResult.MISS;
import static io.rainfall.ehcache.statistics.EhcacheResult.PUT;
import static io.rainfall.ehcache3.CacheConfig.cacheConfig;
import static io.rainfall.ehcache3.CacheDefinition.cache;
import static io.rainfall.ehcache3.Ehcache3Operations.get;
import static io.rainfall.ehcache3.Ehcache3Operations.put;
import static io.rainfall.ehcache3.Ehcache3Operations.putIfAbsent;
import static io.rainfall.ehcache3.Ehcache3Operations.remove;
import static io.rainfall.ehcache3.Ehcache3Operations.removeForKeyAndValue;
import static io.rainfall.execution.Executions.during;
import static io.rainfall.execution.Executions.once;
import static io.rainfall.execution.Executions.ramp;
import static io.rainfall.execution.Executions.times;
import static io.rainfall.generator.ByteArrayGenerator.fixedLengthByteArray;
import static io.rainfall.generator.SequencesGenerator.atRandom;
import static io.rainfall.generator.SequencesGenerator.sequentially;
import static io.rainfall.generator.StringGenerator.fixedLengthString;
import static io.rainfall.generator.sequence.Distribution.FLAT;
import static io.rainfall.generator.sequence.Distribution.GAUSSIAN;
import static io.rainfall.unit.From.from;
import static io.rainfall.unit.Instance.instances;
import static io.rainfall.unit.Over.over;
import static io.rainfall.unit.TimeDivision.minutes;
import static io.rainfall.unit.TimeDivision.seconds;
import static io.rainfall.unit.To.to;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static java.util.concurrent.TimeUnit.MINUTES;
import static org.ehcache.config.builders.CacheManagerBuilder.newCacheManagerBuilder;
import static org.ehcache.config.builders.ResourcePoolsBuilder.newResourcePoolsBuilder;

/**
 * @author Aurelien Broszniowski
 */
@Category(SystemTest.class)
public class PerfTest3 {

  @Test
  @Ignore
  public void testVerifiedValue() {
    ConcurrencyConfig concurrency = ConcurrencyConfig.concurrencyConfig().threads(16).timeout(30, MINUTES);

    ObjectGenerator<Long> keyGenerator = new LongGenerator();
    ObjectGenerator<VerifiedValue> valueGenerator = new VerifiedValueGenerator<Long>(keyGenerator);
    long nbElements = 100;
    CacheConfigurationBuilder<Long, VerifiedValue> builder = CacheConfigurationBuilder.newCacheConfigurationBuilder(Long.class, VerifiedValue.class,
        newResourcePoolsBuilder().heap(nbElements, EntryUnit.ENTRIES)
            .build());

    CacheManager cacheManager = newCacheManagerBuilder()
        .withCache("one", builder.build())
        .build(true);

    Cache<Long, VerifiedValue> one = cacheManager.getCache("one", Long.class, VerifiedValue.class);

    try {
      long start = System.nanoTime();

      System.out.println("Cache Warmup");
      Runner.setUp(
          scenario("Cache warm up phase")
              .exec(
                  put(keyGenerator, valueGenerator, sequentially(), singletonList(cache("one", one)))
              ))
          .executed(times(nbElements))
          .config(report(EhcacheResult.class))
          .config(concurrency)
          .config(cacheConfig(Long.class, VerifiedValue.class).cache("one", one)
          )
          .start();
      long end = System.nanoTime();
      System.out.println("Warmup length : " + (end - start) / 1000000L + "ms");


      System.out.println("Cache Test");
      StatisticsPeekHolder finalStats = Runner.setUp(
          scenario("Cache test phase")
              .exec(
                  weighted(0.10,
                      put(keyGenerator, valueGenerator, sequentially(), true, singletonList(cache("one", one)))
                  ),
                  weighted(0.90,
                      get(keyGenerator, sequentially(), singletonList(cache("one", one))
                      ))))
//          .warmup(during(1, TimeDivision.minutes))
          .executed(during(20, seconds))
          .config(concurrency)
          .config(report(EhcacheResult.class, new EhcacheResult[] { GET, PUT, MISS }).log(text()))
          .config(cacheConfig(Long.class, VerifiedValue.class).cache("one", one))
          .start();

      System.out.println("Nb errors : " + finalStats.getTotalAssertionsErrorsCount());

    } catch (SyntaxException e) {
      e.printStackTrace();
    } finally {
      cacheManager.close();
    }
  }

  @Test
  @Ignore
  public void testKeys() {
    ConcurrencyConfig concurrency = ConcurrencyConfig.concurrencyConfig().threads(16).timeout(30, MINUTES);

    ObjectGenerator<Long> keyGenerator = new ObjectGenerator<Long>() {
      @Override
      public Long generate(final Long seed) {
        System.out.println("seed = " + seed);
        return seed;
      }

      @Override
      public String getDescription() {
        return "";
      }
    };
    ObjectGenerator<byte[]> valueGenerator = fixedLengthByteArray(1024);

    long nbElements = 100;
    CacheConfigurationBuilder<Long, byte[]> builder = CacheConfigurationBuilder.newCacheConfigurationBuilder(Long.class, byte[].class,
        newResourcePoolsBuilder().heap(nbElements, EntryUnit.ENTRIES).build());

    CacheManager cacheManager = newCacheManagerBuilder()
        .withCache("one", builder.build())
        .build(true);

    Cache<Long, byte[]> one = cacheManager.getCache("one", Long.class, byte[].class);

    try {
      long start = System.nanoTime();

      System.out.println("Warmup");
      Runner.setUp(
          scenario("Cache warm up phase")
              .exec(put(keyGenerator, valueGenerator, sequentially(), singletonList(cache("one", one)))))
          .executed(times(nbElements))
          .config(concurrency)
          .config(report(EhcacheResult.class, new EhcacheResult[] { PUT }).log(text()))
          .config(cacheConfig(Long.class, byte[].class)
              .cache("one", one)
          )
          .start();

      long end = System.nanoTime();

      System.out.println("verifying values");
      for (long seed = 2; seed < nbElements; seed++) {
        Object o = one.get(keyGenerator.generate(seed));
        if (o == null) System.out.println("null for key " + seed);
      }
      System.out.println("----------> Done");
    } catch (SyntaxException e) {
      e.printStackTrace();
    } finally {
      cacheManager.close();
    }

  }

  @Test
  @Ignore
  public void testTpsLimit() throws SyntaxException {
    CacheConfigurationBuilder<Long, byte[]> builder = CacheConfigurationBuilder.newCacheConfigurationBuilder(Long.class, byte[].class,
        newResourcePoolsBuilder().heap(250000, EntryUnit.ENTRIES).build());

    final CacheManager cacheManager = newCacheManagerBuilder()
        .withCache("one", builder.build())
        .build(true);

    final Cache<Long, byte[]> one = cacheManager.getCache("one", Long.class, byte[].class);

    ConcurrencyConfig concurrency = ConcurrencyConfig.concurrencyConfig().threads(4).timeout(50, MINUTES);

    ObjectGenerator<Long> keyGenerator = new LongGenerator();
    ObjectGenerator<byte[]> valueGenerator = fixedLengthByteArray(1000);

    EhcacheResult[] resultsReported = new EhcacheResult[] { GET, PUT, MISS };

    Scenario scenario = scenario("Test phase").exec(
        put(keyGenerator, valueGenerator, sequentially(), 50000, singletonList(cache("one", one)))
    );

    System.out.println("----------> Test phase");
    Runner.setUp(scenario)
        .executed(once(4, instances), during(10, seconds))
        .config(concurrency,
            ReportingConfig.report(EhcacheResult.class, resultsReported)
                .log(text(), html()))
        .config(cacheConfig(Long.class, byte[].class).cache("one", one)
        )
        .start();
    System.out.println("----------> Done");

    cacheManager.close();
  }

  @Test
  @Ignore
  public void testWarmup() throws SyntaxException {
    CacheConfigurationBuilder<Long, byte[]> builder = CacheConfigurationBuilder.newCacheConfigurationBuilder(Long.class, byte[].class,
        newResourcePoolsBuilder().heap(250000, EntryUnit.ENTRIES).build());

    final CacheManager cacheManager = newCacheManagerBuilder()
        .withCache("one", builder.build())
        .build(true);

    final Cache<Long, byte[]> one = cacheManager.getCache("one", Long.class, byte[].class);
    final Cache<Long, byte[]> two = cacheManager.getCache("two", Long.class, byte[].class);

    ConcurrencyConfig concurrency = ConcurrencyConfig.concurrencyConfig().threads(4).timeout(50, MINUTES);

    ObjectGenerator<Long> keyGenerator = new LongGenerator();
    ObjectGenerator<byte[]> valueGenerator = fixedLengthByteArray(1000);

    EhcacheResult[] resultsReported = new EhcacheResult[] { GET, PUT, MISS };

    Scenario scenario = scenario("Test phase").exec(
        put(keyGenerator, valueGenerator, sequentially(), singletonList(cache("one", one))),
        get(Long.class, byte[].class).using(keyGenerator, valueGenerator).sequentially()
    );

    System.out.println("----------> Test phase");
    StatisticsPeekHolder finalStats = Runner.setUp(
        scenario)
        .warmup(during(25, seconds))
        .executed(during(30, seconds))
        .config(concurrency,
            ReportingConfig.report(EhcacheResult.class, resultsReported).log(text(), html()))
        .config(cacheConfig(Long.class, byte[].class).cache("one", one)
        )
        .start();
    System.out.println("----------> Done");

    cacheManager.close();
  }


  @Test
  @Ignore
  public void testHisto() throws SyntaxException {
    CacheConfigurationBuilder<Long, byte[]> builder = CacheConfigurationBuilder.newCacheConfigurationBuilder(Long.class, byte[].class,
        newResourcePoolsBuilder().heap(250000, EntryUnit.ENTRIES).build());

    final CacheManager cacheManager = newCacheManagerBuilder()
        .withCache("one", builder.build())
        .withCache("two", builder.build())
        .build(true);

    final Cache<Long, byte[]> one = cacheManager.getCache("one", Long.class, byte[].class);
    final Cache<Long, byte[]> two = cacheManager.getCache("two", Long.class, byte[].class);

    ConcurrencyConfig concurrency = ConcurrencyConfig.concurrencyConfig()
        .threads(4).timeout(50, MINUTES);

    ObjectGenerator<Long> keyGenerator = new LongGenerator();
    ObjectGenerator<byte[]> valueGenerator = fixedLengthByteArray(1000);

    EhcacheResult[] resultsReported = new EhcacheResult[] { GET, PUT, MISS };

    Scenario scenario = scenario("Test phase").exec(
        put(keyGenerator, valueGenerator, sequentially(), asList(cache("one", one), cache("two", two))),
        get(Long.class, byte[].class).using(keyGenerator, valueGenerator).sequentially()
    );

    System.out.println("----------> Warm up phase");
    Runner.setUp(scenario)
        .executed(during(15, seconds))
        .config(concurrency,
            ReportingConfig.report(EhcacheResult.class, resultsReported).log(text()))
        .config(cacheConfig(Long.class, byte[].class).cache("one", one).cache("two", two)
        )
        .start();

    System.out.println("----------> Test phase");
    Runner.setUp(
        scenario)
        .executed(during(30, seconds))
        .config(concurrency,
            ReportingConfig.report(EhcacheResult.class, resultsReported).log(text(), html()))
        .config(cacheConfig(Long.class, byte[].class).cache("one", one).cache("two", two)
        )
        .start();
    System.out.println("----------> Done");

    cacheManager.close();
  }

  @Test
  @Ignore
  public void testLoad() throws SyntaxException {
    int nbCaches = 1;
    int nbElements = 500000;
    CacheConfigurationBuilder<Long, byte[]> configurationBuilder = CacheConfigurationBuilder.newCacheConfigurationBuilder(Long.class, byte[].class,
        newResourcePoolsBuilder().heap(nbElements, EntryUnit.ENTRIES).build());

    CacheManagerBuilder<CacheManager> cacheManagerBuilder = newCacheManagerBuilder();
    for (int i = 0; i < nbCaches; i++) {
      cacheManagerBuilder = cacheManagerBuilder.withCache("cache" + i, configurationBuilder.build());
    }
    CacheManager cacheManager = cacheManagerBuilder.build(true);

    CacheDefinition<Long, byte[]>[] cacheDefinitions = new CacheDefinition[nbCaches];
    for (int i = 0; i < nbCaches; i++) {
      String alias = "cache" + i;
      cacheDefinitions[i] = new CacheDefinition<Long, byte[]>(alias, cacheManager.getCache(alias, Long.class, byte[].class));
    }

    ConcurrencyConfig concurrency = ConcurrencyConfig.concurrencyConfig()
        .threads(40).timeout(50, MINUTES);

    ObjectGenerator<Long> keyGenerator = new LongGenerator();
    ObjectGenerator<byte[]> valueGenerator = fixedLengthByteArray(10);

    System.out.println("----------> Load phase " + new Date());
    ScenarioRun run = Runner.setUp(
            scenario("load phase").exec(
                put(keyGenerator, valueGenerator, atRandom(FLAT, 0, nbElements, nbElements / 10), asList(cacheDefinitions)),
                get(keyGenerator, atRandom(FLAT, 0, nbElements, nbElements / 10), asList(cacheDefinitions)),
                remove(keyGenerator, atRandom(FLAT, 0, nbElements, nbElements / 10), asList(cacheDefinitions)),
                putIfAbsent(keyGenerator, valueGenerator, atRandom(FLAT, 0, nbElements, nbElements / 10), asList(cacheDefinitions))
            ))
        .executed(ramp(from(1, instances), to(40, instances), over(1, minutes)))
        .config(concurrency, ReportingConfig.report(EhcacheResult.class).log(text(), hlog("loadtest")));
    run.start();

    GraphLayout graphLayout = GraphLayout.parseInstance(run);
    System.out.println(graphLayout.totalSize());
    System.out.println("----------> Done " + new Date());

    cacheManager.close();
  }

  @Test
  @Ignore
  public void testReplace() throws SyntaxException {
    CacheConfigurationBuilder<Long, Long> builder = CacheConfigurationBuilder.newCacheConfigurationBuilder(Long.class, Long.class,
        newResourcePoolsBuilder().heap(250000, EntryUnit.ENTRIES).build());

    final CacheManager cacheManager = newCacheManagerBuilder()
        .withCache("one", builder.build())
        .build(true);

    final Cache<Long, Long> one = cacheManager.getCache("one", Long.class, Long.class);

    ConcurrencyConfig concurrency = ConcurrencyConfig.concurrencyConfig()
        .threads(4).timeout(50, MINUTES);

    int nbElements = 250000;
    ObjectGenerator<Long> keyGenerator = new LongGenerator();
    ObjectGenerator<Long> valueGenerator = new LongGenerator();

    ReportingConfig reportingConfig = ReportingConfig.report(EhcacheResult.class).log(text());
    CacheConfig<Long, Long> cacheConfig = cacheConfig(Long.class, Long.class).cache("one", one);
    Runner.setUp(
        scenario("warmup phase").exec(
            put(keyGenerator, valueGenerator, sequentially(), singletonList(cache("one", one)
            ))))
        .executed(times(nbElements))
        .config(concurrency, reportingConfig)
        .config(cacheConfig)
        .start()
    ;
    Runner.setUp(
        scenario("Test phase").exec(
            removeForKeyAndValue(Long.class, Long.class).using(keyGenerator, valueGenerator).sequentially()
        ))
        .executed(during(3, minutes))
        .config(concurrency, reportingConfig)
        .config(cacheConfig)
        .start()
    ;
    cacheManager.close();
  }

  @Test
  @Ignore
  public void testMemory() throws SyntaxException {
    int nbElements = 5000000;
    CacheConfigurationBuilder<Long, Long> builder = CacheConfigurationBuilder.newCacheConfigurationBuilder(Long.class, Long.class,
        newResourcePoolsBuilder().heap(nbElements, EntryUnit.ENTRIES).build());

    final CacheManager cacheManager = newCacheManagerBuilder()
        .withCache("one", builder.build())
        .build(true);

    final Cache<Long, Long> one = cacheManager.getCache("one", Long.class, Long.class);

    ConcurrencyConfig concurrency = ConcurrencyConfig.concurrencyConfig()
        .threads(4).timeout(30, MINUTES);

    ObjectGenerator<Long> keyGenerator = new LongGenerator();
    ObjectGenerator<Long> valueGenerator = new LongGenerator();

    ReportingConfig reportingConfig = ReportingConfig.report(EhcacheResult.class).log(text());
    CacheConfig<Long, Long> cacheConfig = cacheConfig(Long.class, Long.class).cache("one", one);

    Runner.setUp(
        scenario("Test phase").exec(
            put(keyGenerator, valueGenerator, atRandom(GAUSSIAN, 0, nbElements, nbElements / 10), singletonList(cache("one", one)))
        ))
        .executed(during(10, minutes))
        .config(concurrency, reportingConfig)
        .config(cacheConfig)
        .start()
    ;
    cacheManager.close();
  }

  @Test
  @Ignore
  public void tier() {
    int heap = 200000;
    int offheap = 1;
    int disk = 2;

    long nbElementsHeap = MemoryUnit.MB.toBytes(heap) / MemoryUnit.KB.toBytes(1);
    long nbElements = MemoryUnit.GB.toBytes(disk) / MemoryUnit.KB.toBytes(1);

    CacheConfigurationBuilder<String, byte[]> cacheBuilder = CacheConfigurationBuilder.newCacheConfigurationBuilder(String.class, byte[].class,
        newResourcePoolsBuilder()
            .heap(nbElementsHeap, EntryUnit.ENTRIES)
            .offheap(offheap, MemoryUnit.GB)
            .disk(disk, MemoryUnit.GB)
            .build());

    ConcurrencyConfig concurrency = ConcurrencyConfig.concurrencyConfig()
        .threads(4).timeout(30, MINUTES);

    ObjectGenerator<String> keyGenerator = fixedLengthString(10);
    ObjectGenerator<byte[]> valueGenerator = fixedLengthByteArray(1024);

    CacheManager cacheManager = newCacheManagerBuilder()
        .with(new CacheManagerPersistenceConfiguration(new File("/data/PerfTest3")))
        .withCache("one", cacheBuilder.build())
        .build(true);

    Cache<String, byte[]> one = cacheManager.getCache("one", String.class, byte[].class);
    try {
      System.out.println("----------> Cache Warm up phase");
      long start = System.nanoTime();

      Runner.setUp(
          scenario("Cache warm up phase")
              .exec(put(keyGenerator, valueGenerator, sequentially(), singletonList(cache("one", one)))))
          .executed(times(nbElements))
          .config(concurrency)
          .config(report(EhcacheResult.class, new EhcacheResult[] { PUT }).log(text(), html("warmup-tier")))
          .config(cacheConfig(String.class, byte[].class)
              .cache("one", one)
          )
          .start();

      long end = System.nanoTime();
      System.out.println("Warmup time = " + TimeUnit.NANOSECONDS.toMillis((end - start)) + "ms");

      Integer testLength = Integer.parseInt(System.getProperty("testLength", "7"));

      System.out.println("----------> Test phase");
      StatisticsPeekHolder finalStats = Runner.setUp(
          scenario("Test phase")
              .exec(
                  weighted(0.90, get(String.class, byte[].class).using(keyGenerator, valueGenerator)
                      .atRandom(GAUSSIAN, 0, nbElements, nbElements / 10)),
                  weighted(0.10, put(keyGenerator, valueGenerator, atRandom(GAUSSIAN, 0, nbElements, nbElements / 10),
                      singletonList(cache("one", one)))
                  )))
          .warmup(during(3, minutes))
          .executed(during(testLength, minutes))
          .config(concurrency)
          .config(report(EhcacheResult.class, new EhcacheResult[] { PUT, GET, MISS })
              .log(text(), html("test-tier")))
          .config(cacheConfig(String.class, byte[].class)
              .cache("one", one)
          )
          .start();
      System.out.println("----------> Done");
    } catch (SyntaxException e) {
      e.printStackTrace();
    } finally {
      cacheManager.close();
    }
  }


  @Test
  @Ignore
  public void testBasic() {
    int heap = 200000;
    int offheap = 1;
    int disk = 2;

    long nbElementsHeap = MemoryUnit.MB.toBytes(heap) / MemoryUnit.KB.toBytes(1);
    long nbElements = MemoryUnit.GB.toBytes(disk) / MemoryUnit.KB.toBytes(1);

    CacheConfigurationBuilder<String, byte[]> cacheBuilder = CacheConfigurationBuilder.newCacheConfigurationBuilder(String.class, byte[].class,
        newResourcePoolsBuilder()
            .heap(nbElementsHeap, EntryUnit.ENTRIES)
            .offheap(offheap, MemoryUnit.GB)
            .build());

    ConcurrencyConfig concurrency = ConcurrencyConfig.concurrencyConfig()
        .threads(4).timeout(30, MINUTES);

    ObjectGenerator<String> keyGenerator = fixedLengthString(10);
    ObjectGenerator<byte[]> valueGenerator = fixedLengthByteArray(1024);

    CacheManager cacheManager = newCacheManagerBuilder()
        .with(new CacheManagerPersistenceConfiguration(new File("/data/PerfTest3")))
        .withCache("one", cacheBuilder.build())
        .build(true);

    Cache<String, byte[]> one = cacheManager.getCache("one", String.class, byte[].class);
    try {
      System.out.println("----------> Cache Warm up phase");
      long start = System.nanoTime();

      Runner.setUp(
          scenario("Cache warm up phase")
              .exec(put(keyGenerator, valueGenerator, sequentially(), singletonList(cache("one", one)))))
          .executed(times(nbElements))
          .config(concurrency)
          .config(report(EhcacheResult.class, new EhcacheResult[] { PUT }).log(text()))
          .config(cacheConfig(String.class, byte[].class)
              .cache("one", one)
          )
//          .start()
      ;

      long end = System.nanoTime();
      System.out.println("Warmup time = " + TimeUnit.NANOSECONDS.toMillis((end - start)) + "ms");

      Integer testLength = Integer.parseInt(System.getProperty("testLength", "7"));

      System.out.println("----------> Test phase");
      StatisticsPeekHolder finalStats = Runner.setUp(
          scenario("Test phase")
              .exec(
                  weighted(0.90, get(String.class, byte[].class).using(keyGenerator, valueGenerator)
                      .atRandom(GAUSSIAN, 0, nbElements, nbElements / 10)),
                  weighted(0.10, put(keyGenerator, valueGenerator, atRandom(GAUSSIAN, 0, nbElements, nbElements / 10),
                      singletonList(cache("one", one))))
              ))
          .warmup(during(30, seconds))
          .executed(during(2, minutes))
          .config(concurrency)
          .config(report(EhcacheResult.class, new EhcacheResult[] { PUT, GET, MISS })
              .collect(gcStatistics()).log(text(), html("test-basic")))
          .config(cacheConfig(String.class, byte[].class)
              .cache("one", one)
          )
          .start();
      System.out.println("----------> Done");
    } catch (SyntaxException e) {
      e.printStackTrace();
    } finally {
      cacheManager.close();
    }
  }

  @Test
  @Ignore
  public void testClustered() {
    int nbElements = 1000;
    CacheConfigurationBuilder<String, byte[]> cacheBuilder = CacheConfigurationBuilder.newCacheConfigurationBuilder(String.class, byte[].class,
        newResourcePoolsBuilder()
            .heap(nbElements, EntryUnit.ENTRIES)
            .build());

    ConcurrencyConfig concurrency = ConcurrencyConfig.concurrencyConfig()
        .threads(4).timeout(30, MINUTES);

    ObjectGenerator<String> keyGenerator = fixedLengthString(10);
    ObjectGenerator<byte[]> valueGenerator = fixedLengthByteArray(1024);

    CacheManager cacheManager = newCacheManagerBuilder()
        .withCache("one", cacheBuilder.build())
        .build(true);

    Cache<String, byte[]> one = cacheManager.getCache("one", String.class, byte[].class);
    try {

      StatisticsPeekHolder finalStats = Runner.setUp(
          scenario("Test phase")
              .exec(
                  weighted(0.50, get(String.class, byte[].class).using(keyGenerator, valueGenerator)
                      .atRandom(GAUSSIAN, 0, nbElements, nbElements / 10)),
                  weighted(0.50, put(keyGenerator, valueGenerator, atRandom(GAUSSIAN, 0, nbElements, nbElements / 10),
                      singletonList(cache("one", one))))
              ))
          .warmup(during(30, seconds))
          .executed(during(2, minutes))
          .config(concurrency)
          .config(report(EhcacheResult.class, new EhcacheResult[] { PUT, GET, MISS })
              .log(text(), html("test-clustered")))
          .config(cacheConfig(String.class, byte[].class)
              .cache("one", one)
          )
          .start();
    } catch (SyntaxException e) {
      e.printStackTrace();
    } finally {
      cacheManager.close();
    }
  }


  @Test
  @Ignore
  public void testStatisticsCollectors() {
    int nbElements = 1000;

    CacheConfigurationBuilder<String, byte[]> cacheBuilder = CacheConfigurationBuilder.newCacheConfigurationBuilder(String.class, byte[].class,
        newResourcePoolsBuilder()
            .heap(nbElements, EntryUnit.ENTRIES)
            .build());

    ConcurrencyConfig concurrency = ConcurrencyConfig.concurrencyConfig()
        .threads(4).timeout(30, MINUTES);

    ObjectGenerator<String> keyGenerator = fixedLengthString(10);
    ObjectGenerator<byte[]> valueGenerator = fixedLengthByteArray(10);

    CacheManager cacheManager = newCacheManagerBuilder()
        .withCache("one", cacheBuilder.build())
        .build(true);

    Cache<String, byte[]> one = cacheManager.getCache("one", String.class, byte[].class);
    try {
      Runner.setUp(
          scenario("Test reporters")
              .exec(
                  put(keyGenerator, valueGenerator, sequentially(), singletonList(cache("one", one))))
      )
          .executed(during(1, minutes))
          .config(concurrency)
          .config(report(EhcacheResult.class, new EhcacheResult[] { PUT })
              .collect(gcStatistics()).log(html()))
          .config(cacheConfig(String.class, byte[].class)
              .cache("one", one)
          )
          .start()
      ;


    } catch (SyntaxException e) {
      e.printStackTrace();
    } finally {
      cacheManager.close();
    }
  }

  @Test
  @Ignore
  public void testPartition() {
    int heap = 200000;
    int offheap = 1;
    int disk = 2;

    long nbElementsHeap = MemoryUnit.MB.toBytes(heap) / MemoryUnit.KB.toBytes(1);
    long nbElements = MemoryUnit.GB.toBytes(disk) / MemoryUnit.KB.toBytes(1);

    CacheConfigurationBuilder<String, byte[]> cacheBuilder = CacheConfigurationBuilder.newCacheConfigurationBuilder(String.class, byte[].class,
        newResourcePoolsBuilder()
            .heap(nbElementsHeap, EntryUnit.ENTRIES)
            .offheap(offheap, MemoryUnit.GB)
            .build());

    CacheManager cacheManager = newCacheManagerBuilder()
        .with(new CacheManagerPersistenceConfiguration(new File("/data/PerfTest3")))
        .withCache("one", cacheBuilder.build())
        .build(true);

    Cache<String, byte[]> one = cacheManager.getCache("one", String.class, byte[].class);

    try {

      System.out.println("----------> Test phase");
      StatisticsPeekHolder finalStats = Runner.setUp(
          scenario("Test phase")
              .exec("POOL1",
                  weighted(0.1,
                      put(fixedLengthString(10), fixedLengthByteArray(1024), atRandom(GAUSSIAN, 0, nbElements, nbElements / 10),
                          singletonList(cache("ee", one)))
                  ),
                  weighted(0.9,
                      get(String.class, byte[].class).using(fixedLengthString(10), fixedLengthByteArray(1024))
                          .atRandom(GAUSSIAN, 0, nbElements, nbElements / 10)
                  )
              ).exec("POOL2",
              remove(String.class, byte[].class).using(fixedLengthString(10), fixedLengthByteArray(1024)).
                  atRandom(GAUSSIAN, 0, nbElements, nbElements / 10)
          )
      )
          .executed(during(2, minutes))
          .config(ConcurrencyConfig.concurrencyConfig()
              .threads("POOL1", 4).threads("POOL2", 4).timeout(30, MINUTES))
          .config(report(EhcacheResult.class, EnumSet.of(PUT, GET, MISS))
              .log(text(), html("test-basic")))
          .config(cacheConfig(String.class, byte[].class)
              .cache("one", one)
          )
          .start();
      System.out.println("----------> Done");
    } catch (SyntaxException e) {
      e.printStackTrace();
    } finally {
      cacheManager.close();
    }
  }

  @Test
  @Ignore
  public void testMultipleCaches() {
    int eltCount = 200000;


    ConcurrencyConfig concurrency = ConcurrencyConfig.concurrencyConfig()
        .threads(4).timeout(30, MINUTES);

    ObjectGenerator<String> onekeyGenerator = fixedLengthString(10);
    ObjectGenerator<Long> twokeyGenerator = new LongGenerator();
    ObjectGenerator<byte[]> valueGenerator = fixedLengthByteArray(1024);

    CacheManager cacheManager = newCacheManagerBuilder()
        .with(new CacheManagerPersistenceConfiguration(new File("/data/PerfTest3")))
        .withCache("one", CacheConfigurationBuilder.newCacheConfigurationBuilder(String.class, byte[].class,
            newResourcePoolsBuilder().heap(eltCount, EntryUnit.ENTRIES).build()).build())
        .withCache("two", CacheConfigurationBuilder.newCacheConfigurationBuilder(Long.class, byte[].class,
            newResourcePoolsBuilder().heap(eltCount, EntryUnit.ENTRIES).build()).build())
        .build(true);

    Cache<String, byte[]> one = cacheManager.getCache("one", String.class, byte[].class);
    Cache<Long, byte[]> two = cacheManager.getCache("two", Long.class, byte[].class);
    try {
      System.out.println("----------> Cache Warm up phase");
      long start = System.nanoTime();

      Runner.setUp(
          scenario("Cache warm up phase")
              .exec(
                  put(onekeyGenerator, valueGenerator, atRandom(GAUSSIAN, 0, eltCount, eltCount), singletonList(cache("one", one))),
                  put(twokeyGenerator, valueGenerator, atRandom(GAUSSIAN, 0, eltCount, eltCount), singletonList(cache("two", two)))
              ))
          .executed(during(2, minutes))
          .config(concurrency)
          .config(report(EhcacheResult.class, new EhcacheResult[] { PUT }).log(text(), html()))
          .start()
      ;

      long end = System.nanoTime();
      System.out.println("Warmup time = " + TimeUnit.NANOSECONDS.toMillis((end - start)) + "ms");

      System.out.println("----------> Done");
    } catch (SyntaxException e) {
      e.printStackTrace();
    } finally {
      cacheManager.close();
    }
  }

  @Test
  @Ignore
  public void testHlog() {
    int heap = 200000;
    int offheap = 1;
    int disk = 2;

    long nbElementsHeap = MemoryUnit.MB.toBytes(heap) / MemoryUnit.KB.toBytes(1);
    long nbElements = MemoryUnit.GB.toBytes(disk) / MemoryUnit.KB.toBytes(1);

    CacheConfigurationBuilder<String, byte[]> cacheBuilder = CacheConfigurationBuilder.newCacheConfigurationBuilder(String.class, byte[].class,
        newResourcePoolsBuilder()
            .heap(nbElementsHeap, EntryUnit.ENTRIES)
            .offheap(offheap, MemoryUnit.GB)
            .build());

    ConcurrencyConfig concurrency = ConcurrencyConfig.concurrencyConfig()
        .threads(4).timeout(30, MINUTES);

    ObjectGenerator<String> keyGenerator = fixedLengthString(10);
    ObjectGenerator<byte[]> valueGenerator = fixedLengthByteArray(1024);

    CacheManager cacheManager = newCacheManagerBuilder()
        .with(new CacheManagerPersistenceConfiguration(new File("/data/PerfTest3")))
        .withCache("one", cacheBuilder.build())
        .build(true);

    Cache<String, byte[]> one = cacheManager.getCache("one", String.class, byte[].class);
    try {
      System.out.println("----------> Test phase");

      Runner.setUp(
          Scenario.scenario("Cache warm up phase")
              .exec(
                  put(keyGenerator, valueGenerator, new IterationSequenceGenerator(), singletonList(cache("one", one)))
              ))
          .warmup(during(30, seconds))
          .executed(during(1, minutes))
          .config(concurrency)
          .config(report(EhcacheResult.class).log(text(), html("report-html"),
              hlog("report-log")))
          .start();

      System.out.println("----------> Done");
    } catch (SyntaxException e) {
      e.printStackTrace();
    } finally {
      cacheManager.close();
    }
  }
}
