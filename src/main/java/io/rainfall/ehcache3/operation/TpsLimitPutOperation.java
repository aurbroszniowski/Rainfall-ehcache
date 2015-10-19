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

package io.rainfall.ehcache3.operation;

import io.rainfall.AssertionEvaluator;
import io.rainfall.Configuration;
import io.rainfall.TestException;
import io.rainfall.ehcache.statistics.EhcacheResult;
import io.rainfall.ehcache3.CacheConfig;
import io.rainfall.statistics.StatisticsHolder;
import org.ehcache.Cache;

import java.util.List;
import java.util.Map;

import static io.rainfall.ehcache.statistics.EhcacheResult.EXCEPTION;
import static io.rainfall.ehcache.statistics.EhcacheResult.PUT;

/**
 * @author Aurelien Broszniowski
 */
public class TpsLimitPutOperation<K, V> extends PutOperation<K, V> {

  private final long tpsLimit;

  public TpsLimitPutOperation(final long tpsLimit) {
    this.tpsLimit = tpsLimit;
  }

  @Override
  public void exec(final StatisticsHolder statisticsHolder, final Map<Class<? extends Configuration>,
      Configuration> configurations, final List<AssertionEvaluator> assertions) throws TestException {

    CacheConfig<K, V> cacheConfig = (CacheConfig<K, V>)configurations.get(CacheConfig.class);
    final long next = this.sequenceGenerator.next();
    List<Cache<K, V>> caches = cacheConfig.getCaches();
    long currentTps = statisticsHolder.getCurrentTps(EhcacheResult.PUT);
    if (currentTps < this.tpsLimit) {
      for (final Cache<K, V> cache : caches) {
        K k = keyGenerator.generate(next);
        V v = valueGenerator.generate(next);

        long start = getTimeInNs();
        try {
          cache.put(k, v);
          long end = getTimeInNs();
          statisticsHolder.record(cacheConfig.getCacheName(cache), (end - start), PUT);
        } catch (Exception e) {
          long end = getTimeInNs();
          statisticsHolder.record(cacheConfig.getCacheName(cache), (end - start), EXCEPTION);
        }
      }
    }
  }
}
