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

package io.rainfall.ehcache2.operation;

import io.rainfall.AssertionEvaluator;
import io.rainfall.Configuration;
import io.rainfall.EhcacheOperation;
import io.rainfall.TestException;
import io.rainfall.ehcache2.CacheConfig;
import io.rainfall.statistics.StatisticsHolder;
import net.sf.ehcache.Ehcache;

import java.util.List;
import java.util.Map;

import static io.rainfall.ehcache.statistics.EhcacheResult.EXCEPTION;
import static io.rainfall.ehcache.statistics.EhcacheResult.MISS;
import static io.rainfall.ehcache.statistics.EhcacheResult.REMOVE;

/**
 * Execute and measure a Ehcache remove operation
 *
 * @author Aurelien Broszniowski
 */
public class RemoveOperation<K, V> extends EhcacheOperation {

  @Override
  public void exec(final StatisticsHolder statisticsHolder, final Map<Class<? extends Configuration>,
      Configuration> configurations, final List<AssertionEvaluator> assertions) throws TestException {

    CacheConfig<K, V> cacheConfig = (CacheConfig<K, V>)configurations.get(CacheConfig.class);
    final long next = this.sequenceGenerator.next();
    List<Ehcache> caches = cacheConfig.getCaches();
    for (final Ehcache cache : caches) {
      boolean removed;
      long start = getTimeInNs();
      try {
        removed = cache.remove(keyGenerator.generate(next));
        long end = getTimeInNs();
        if (removed) {
          statisticsHolder.record(cache.getName(), (end - start), REMOVE);
        } else {
          statisticsHolder.record(cache.getName(), (end - start), MISS);
        }
      } catch (Exception e) {
        long end = getTimeInNs();
        statisticsHolder.record(cache.getName(), (end - start), EXCEPTION);
      }
    }
  }
}
