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
import io.rainfall.generator.StringGenerator;
import io.rainfall.statistics.StatisticsPeekHolder;
import org.ehcache.Cache;
import org.ehcache.CacheManager;
import org.ehcache.config.CacheConfigurationBuilder;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import static io.rainfall.configuration.ReportingConfig.text;
import static io.rainfall.ehcache3.CacheConfig.cacheConfig;
import static io.rainfall.ehcache3.Ehcache3Operations.get;

import static io.rainfall.ehcache3.Ehcache3Operations.put;
import static io.rainfall.ehcache3.Ehcache3Operations.remove;
import static io.rainfall.execution.Executions.nothingFor;
import static io.rainfall.execution.Executions.times;
import static io.rainfall.unit.TimeDivision.seconds;
import static java.util.concurrent.TimeUnit.MINUTES;
import static org.ehcache.CacheManagerBuilder.newCacheManagerBuilder;

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
            .buildConfig(String.class, byte[].class))
        .build(true);

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
  @Ignore
  public void test3() throws SyntaxException {
    CacheConfig<String, byte[]> cacheConfig = cacheConfig(String.class, byte[].class).caches(cache);
    ConcurrencyConfig concurrency = ConcurrencyConfig.concurrencyConfig()
        .threads(4).timeout(5, MINUTES);
    ReportingConfig reporting = ReportingConfig.report(EhcacheResult.class).log(text());

    ObjectGenerator<String> keyGenerator = StringGenerator.fixedLength(10);
    ObjectGenerator<byte[]> valueGenerator = ByteArrayGenerator.fixedLength(128);
    Scenario scenario = Scenario.scenario("Cache load")
        .exec(
            put(String.class, byte[].class)
                .withWeight(0.10)
                .using(keyGenerator, valueGenerator)
                .sequentially(),
            get(String.class, byte[].class)
                .withWeight(0.80)
                .using(keyGenerator, valueGenerator)
                .sequentially(),
            remove(String.class, byte[].class)
                .withWeight(0.10)
                .using(keyGenerator, valueGenerator)
                .sequentially()
        );

    StatisticsPeekHolder finalStats = Runner.setUp(scenario)
        .executed(times(1000), nothingFor(10, seconds))
        .config(cacheConfig, concurrency, reporting)
//          .assertion(latencyTime(), isLessThan(1, seconds))
        .start();
  }
}
