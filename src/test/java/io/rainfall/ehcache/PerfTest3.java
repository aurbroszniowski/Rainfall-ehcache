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
import io.rainfall.ehcache3.operation.PutVerifiedOperation;
import io.rainfall.generator.ByteArrayGenerator;
import io.rainfall.generator.LongGenerator;
import io.rainfall.generator.StringGenerator;
import io.rainfall.generator.VerifiedValueGenerator;
import io.rainfall.generator.VerifiedValueGenerator.VerifiedValue;
import io.rainfall.generator.sequence.Distribution;
import io.rainfall.statistics.StatisticsPeekHolder;
import io.rainfall.utils.SystemTest;
import org.ehcache.Cache;
import org.ehcache.CacheManager;
import org.ehcache.config.builders.CacheConfigurationBuilder;
import org.ehcache.config.builders.CacheManagerConfiguration;
import org.ehcache.config.units.EntryUnit;
import org.ehcache.config.units.MemoryUnit;
import org.ehcache.impl.config.persistence.CacheManagerPersistenceConfiguration;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.io.File;
import java.util.concurrent.TimeUnit;

import static io.rainfall.configuration.ReportingConfig.gcStatistics;
import static io.rainfall.configuration.ReportingConfig.html;
import static io.rainfall.configuration.ReportingConfig.report;
import static io.rainfall.configuration.ReportingConfig.text;
import static io.rainfall.ehcache.statistics.EhcacheResult.GET;
import static io.rainfall.ehcache.statistics.EhcacheResult.MISS;
import static io.rainfall.ehcache.statistics.EhcacheResult.PUT;
import static io.rainfall.ehcache.statistics.EhcacheResult.PUTALL;
import static io.rainfall.ehcache3.CacheConfig.cacheConfig;
import static io.rainfall.ehcache3.Ehcache3Operations.get;
import static io.rainfall.ehcache3.Ehcache3Operations.put;
import static io.rainfall.ehcache3.Ehcache3Operations.removeForKeyAndValue;
import static io.rainfall.execution.Executions.during;
import static io.rainfall.execution.Executions.once;
import static io.rainfall.execution.Executions.times;
import static io.rainfall.generator.sequence.Distribution.GAUSSIAN;
import static io.rainfall.unit.Instance.instances;
import static io.rainfall.unit.TimeDivision.minutes;
import static io.rainfall.unit.TimeDivision.seconds;
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
          Scenario.scenario("Cache warm up phase")
              .exec(
                  put(Long.class, VerifiedValue.class).using(keyGenerator, valueGenerator).sequentially()))
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
          Scenario.scenario("Cache test phase")
              .exec(
                  new PutVerifiedOperation(Long.class, VerifiedValue.class).using(keyGenerator, valueGenerator)
                      .sequentially()
                      .withWeight(0.10),
                  get(Long.class, VerifiedValue.class).using(keyGenerator, valueGenerator)
                      .sequentially()
                      .withWeight(0.90)))
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
    ObjectGenerator<byte[]> valueGenerator = ByteArrayGenerator.fixedLength(1024);

    long nbElements = 100;
    CacheConfigurationBuilder<Long, Byte[]> builder = CacheConfigurationBuilder.newCacheConfigurationBuilder(Long.class, Byte[].class,
        newResourcePoolsBuilder().heap(nbElements, EntryUnit.ENTRIES).build());

    CacheManager cacheManager = newCacheManagerBuilder()
        .withCache("one", builder.build())
        .build(true);

    Cache<Long, Byte[]> one = cacheManager.getCache("one", Long.class, Byte[].class);

    try {
      long start = System.nanoTime();

      System.out.println("Warmup");
      Runner.setUp(
          Scenario.scenario("Cache warm up phase")
              .exec(put(Long.class, byte[].class).using(keyGenerator, valueGenerator).sequentially()))
          .executed(times(nbElements))
          .config(concurrency)
          .config(report(EhcacheResult.class, new EhcacheResult[] { PUT }).log(text()))
          .config(cacheConfig(Long.class, Byte[].class)
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
    CacheConfigurationBuilder<Long, Byte[]> builder = CacheConfigurationBuilder.newCacheConfigurationBuilder(Long.class, Byte[].class,
        newResourcePoolsBuilder().heap(250000, EntryUnit.ENTRIES).build());

    final CacheManager cacheManager = newCacheManagerBuilder()
        .withCache("one", builder.build())
        .build(true);

    final Cache<Long, Byte[]> one = cacheManager.getCache("one", Long.class, Byte[].class);

    ConcurrencyConfig concurrency = ConcurrencyConfig.concurrencyConfig().threads(4).timeout(50, MINUTES);

    ObjectGenerator<Long> keyGenerator = new LongGenerator();
    ObjectGenerator<byte[]> valueGenerator = ByteArrayGenerator.fixedLength(1000);

    EhcacheResult[] resultsReported = new EhcacheResult[] { GET, PUT, MISS };

    Scenario scenario = Scenario.scenario("Test phase").exec(
        put(Long.class, byte[].class, 50000).using(keyGenerator, valueGenerator).sequentially()
    );

    System.out.println("----------> Test phase");
    Runner.setUp(scenario)
        .executed(once(4, instances), during(10, seconds))
        .config(concurrency,
            ReportingConfig.report(EhcacheResult.class, resultsReported)
                .log(text(), html()))
        .config(cacheConfig(Long.class, Byte[].class).cache("one", one)
        )
        .start();
    System.out.println("----------> Done");

    cacheManager.close();
  }

  @Test
  @Ignore
  public void testWarmup() throws SyntaxException {
    CacheConfigurationBuilder<Long, Byte[]> builder = CacheConfigurationBuilder.newCacheConfigurationBuilder(Long.class, Byte[].class,
        newResourcePoolsBuilder().heap(250000, EntryUnit.ENTRIES).build());

    final CacheManager cacheManager = newCacheManagerBuilder()
        .withCache("one", builder.build())
        .build(true);

    final Cache<Long, Byte[]> one = cacheManager.getCache("one", Long.class, Byte[].class);
    final Cache<Long, Byte[]> two = cacheManager.getCache("two", Long.class, Byte[].class);

    ConcurrencyConfig concurrency = ConcurrencyConfig.concurrencyConfig().threads(4).timeout(50, MINUTES);

    ObjectGenerator<Long> keyGenerator = new LongGenerator();
    ObjectGenerator<byte[]> valueGenerator = ByteArrayGenerator.fixedLength(1000);

    EhcacheResult[] resultsReported = new EhcacheResult[] { GET, PUT, MISS };

    Scenario scenario = Scenario.scenario("Test phase").exec(
        put(Long.class, byte[].class).using(keyGenerator, valueGenerator).sequentially(),
        get(Long.class, byte[].class).using(keyGenerator, valueGenerator).sequentially()
    );

    System.out.println("----------> Test phase");
    StatisticsPeekHolder finalStats = Runner.setUp(
        scenario)
        .warmup(during(25, seconds))
        .executed(during(30, seconds))
        .config(concurrency,
            ReportingConfig.report(EhcacheResult.class, resultsReported).log(text(), html()))
        .config(cacheConfig(Long.class, Byte[].class).cache("one", one)
        )
        .start();
    System.out.println("----------> Done");

    cacheManager.close();
  }


  @Test
  @Ignore
  public void testHisto() throws SyntaxException {
    CacheConfigurationBuilder<Long, Byte[]> builder = CacheConfigurationBuilder.newCacheConfigurationBuilder(Long.class, Byte[].class,
        newResourcePoolsBuilder().heap(250000, EntryUnit.ENTRIES).build());

    final CacheManager cacheManager = newCacheManagerBuilder()
        .withCache("one", builder.build())
        .withCache("two", builder.build())
        .build(true);

    final Cache<Long, Byte[]> one = cacheManager.getCache("one", Long.class, Byte[].class);
    final Cache<Long, Byte[]> two = cacheManager.getCache("two", Long.class, Byte[].class);

    ConcurrencyConfig concurrency = ConcurrencyConfig.concurrencyConfig()
        .threads(4).timeout(50, MINUTES);

    ObjectGenerator<Long> keyGenerator = new LongGenerator();
    ObjectGenerator<byte[]> valueGenerator = ByteArrayGenerator.fixedLength(1000);

    EhcacheResult[] resultsReported = new EhcacheResult[] { GET, PUT, MISS };

    Scenario scenario = Scenario.scenario("Test phase").exec(
        put(Long.class, byte[].class).using(keyGenerator, valueGenerator).sequentially(),
        get(Long.class, byte[].class).using(keyGenerator, valueGenerator).sequentially()
    );

    System.out.println("----------> Warm up phase");
    Runner.setUp(scenario)
        .executed(during(15, seconds))
        .config(concurrency,
            ReportingConfig.report(EhcacheResult.class, resultsReported).log(text()))
        .config(cacheConfig(Long.class, Byte[].class).cache("one", one).cache("two", two)
        )
        .start();

    System.out.println("----------> Test phase");
    Runner.setUp(
        scenario)
        .executed(during(30, seconds))
        .config(concurrency,
            ReportingConfig.report(EhcacheResult.class, resultsReported).log(text(), html()))
        .config(cacheConfig(Long.class, Byte[].class).cache("one", one).cache("two", two)
        )
        .start();
    System.out.println("----------> Done");

    cacheManager.close();
  }

  @Test
  @Ignore
  public void testLoad() throws SyntaxException {
    CacheConfigurationBuilder<Long, Byte[]> builder = CacheConfigurationBuilder.newCacheConfigurationBuilder(Long.class, Byte[].class,
        newResourcePoolsBuilder().heap(250000, EntryUnit.ENTRIES).build());

    final CacheManager cacheManager = newCacheManagerBuilder()
        .withCache("one", builder.build())
        .withCache("two", builder.build())
        .withCache("three", builder.build())
        .withCache("four", builder.build())
        .build(true);

    final Cache<Long, Byte[]> one = cacheManager.getCache("one", Long.class, Byte[].class);
    final Cache<Long, Byte[]> two = cacheManager.getCache("two", Long.class, Byte[].class);
    final Cache<Long, Byte[]> three = cacheManager.getCache("three", Long.class, Byte[].class);
    final Cache<Long, Byte[]> four = cacheManager.getCache("four", Long.class, Byte[].class);

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
        .config(cacheConfig(Long.class, Byte[].class)
            .cache("one", one).cache("two", two).cache("three", three).cache("four", four)
            .bulkBatchSize(5)
        )
        .start()
    ;

    try {
      Thread.sleep(1000);
    } catch (InterruptedException e) {
      e.printStackTrace();
    }

//    System.out.println(one.getStatistics().getCachePuts());
//    System.out.println(two.getStatistics().getCachePuts());
//    System.out.println(three.getStatistics().getCachePuts());
//    System.out.println(four.getStatistics().getCachePuts());

    System.out.println("----------> Test phase");

    StatisticsPeekHolder finalStats = Runner.setUp(
        Scenario.scenario("Test phase").exec(
            put(Long.class, byte[].class).withWeight(0.10)
                .using(keyGenerator, valueGenerator)
                .atRandom(GAUSSIAN, 0, nbElements, 10000),
            get(Long.class, byte[].class).withWeight(0.80)
                .using(keyGenerator, valueGenerator)
                .atRandom(GAUSSIAN, 0, nbElements, 10000)
//            putAll(Long.class, Byte[].class).withWeight(0.10)
//                .using(keyGenerator, valueGenerator)
//                .atRandom(GAUSSIAN, 0, nbElements, 10000),
//            getAll(Long.class, Byte[].class).withWeight(0.40)
//                .using(keyGenerator, valueGenerator)
//                .atRandom(GAUSSIAN, 0, nbElements, 10000)
//            removeAll(Long.class, Byte[].class).withWeight(0.10)
//                .using(keyGenerator, valueGenerator)
//                .atRandom(GAUSSIAN, 0, nbElements, 10000),
//            putIfAbsent(Long.class, Byte[].class).withWeight(0.10)
//                .using(keyGenerator, valueGenerator)
//                .atRandom(GAUSSIAN, 0, nbElements, 10000),
//            replace(Long.class, Byte[].class).withWeight(0.10)
//                .using(keyGenerator, valueGenerator)
//                .atRandom(GAUSSIAN, 0, nbElements, 10000)
//            replaceForKeyAndValue(Long.class, Byte[].class).withWeight(0.10)
//                .using(keyGenerator, valueGenerator)
//                .atRandom(GAUSSIAN, 0, nbElements, 10000)
        ))
        .executed(during(1, minutes))
        .config(concurrency, ReportingConfig.report(EhcacheResult.class).log(text(), html()))
        .config(cacheConfig(Long.class, Byte[].class)
            .cache("one", one).cache("two", two).cache("three", three).cache("four", four)
            .bulkBatchSize(10))
        .start();

    System.out.println("----------> Done");

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
        Scenario.scenario("Test phase").exec(
            put(Long.class, Long.class).using(keyGenerator, valueGenerator)
                .atRandom(GAUSSIAN, 0, nbElements, nbElements / 10)
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

    ObjectGenerator<String> keyGenerator = StringGenerator.fixedLength(10);
    ObjectGenerator<byte[]> valueGenerator = ByteArrayGenerator.fixedLength(1024);

    CacheManager cacheManager = newCacheManagerBuilder()
        .with(new CacheManagerPersistenceConfiguration(new File("/data/PerfTest3")))
        .withCache("one", cacheBuilder.build())
        .build(true);

    Cache one = cacheManager.getCache("one", String.class, byte[].class);
    try {
      System.out.println("----------> Cache Warm up phase");
      long start = System.nanoTime();

      Runner.setUp(
          Scenario.scenario("Cache warm up phase")
              .exec(put(String.class, byte[].class).using(keyGenerator, valueGenerator).sequentially()))
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
          Scenario.scenario("Test phase")
              .exec(
                  get(String.class, byte[].class).using(keyGenerator, valueGenerator)
                      .atRandom(Distribution.GAUSSIAN, 0, nbElements, nbElements / 10)
                      .withWeight(0.90),
                  put(String.class, byte[].class).using(keyGenerator, valueGenerator)
                      .atRandom(Distribution.GAUSSIAN, 0, nbElements, nbElements / 10)
                      .withWeight(0.10)
              ))
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

    ObjectGenerator<String> keyGenerator = StringGenerator.fixedLength(10);
    ObjectGenerator<byte[]> valueGenerator = ByteArrayGenerator.fixedLength(1024);

    CacheManager cacheManager = newCacheManagerBuilder()
        .with(new CacheManagerPersistenceConfiguration(new File("/data/PerfTest3")))
        .withCache("one", cacheBuilder.build())
        .build(true);

    Cache one = cacheManager.getCache("one", String.class, byte[].class);
    try {
      System.out.println("----------> Cache Warm up phase");
      long start = System.nanoTime();

      Runner.setUp(
          Scenario.scenario("Cache warm up phase")
              .exec(put(String.class, byte[].class).using(keyGenerator, valueGenerator).sequentially()))
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
          Scenario.scenario("Test phase")
              .exec(
                  get(String.class, byte[].class).using(keyGenerator, valueGenerator)
                      .atRandom(Distribution.GAUSSIAN, 0, nbElements, nbElements / 10)
                      .withWeight(0.90),
                  put(String.class, byte[].class).using(keyGenerator, valueGenerator)
                      .atRandom(Distribution.GAUSSIAN, 0, nbElements, nbElements / 10)
                      .withWeight(0.10)
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

    ObjectGenerator<String> keyGenerator = StringGenerator.fixedLength(10);
    ObjectGenerator<byte[]> valueGenerator = ByteArrayGenerator.fixedLength(1024);

    CacheManager cacheManager = newCacheManagerBuilder()
        .withCache("one", cacheBuilder.build())
        .build(true);

    Cache<String, byte[]> one = cacheManager.getCache("one", String.class, byte[].class);
    try {

      StatisticsPeekHolder finalStats = Runner.setUp(
          Scenario.scenario("Test phase")
              .exec(
                  get(String.class, byte[].class).using(keyGenerator, valueGenerator)
                      .atRandom(Distribution.GAUSSIAN, 0, nbElements, nbElements / 10)
                      .withWeight(0.50),
                  put(String.class, byte[].class).using(keyGenerator, valueGenerator)
                      .atRandom(Distribution.GAUSSIAN, 0, nbElements, nbElements / 10)
                      .withWeight(0.50)
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

    ObjectGenerator<String> keyGenerator = StringGenerator.fixedLength(10);
    ObjectGenerator<byte[]> valueGenerator = ByteArrayGenerator.fixedLength(10);

    CacheManager cacheManager = newCacheManagerBuilder()
        .withCache("one", cacheBuilder.build())
        .build(true);

    Cache one = cacheManager.getCache("one", String.class, byte[].class);
    try {
      Runner.setUp(
          Scenario.scenario("Test reporters")
              .exec(
                  put(String.class, byte[].class)
                      .withWeight(1.0)
                      .using(keyGenerator, valueGenerator).sequentially())
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
}
