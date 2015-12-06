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
import io.rainfall.EhcacheOperation;
import io.rainfall.TestException;
import io.rainfall.ehcache3.CacheConfig;
import io.rainfall.statistics.StatisticsHolder;
import org.ehcache.Cache;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;

import static io.rainfall.ehcache.statistics.EhcacheResult.EXCEPTION;
import static io.rainfall.ehcache.statistics.EhcacheResult.REMOVEALL;

/**
 * @author Aurelien Broszniowski
 */
public class RemoveAllOperation<K, V> extends EhcacheOperation<K, V> {

  @Override
  public void exec(final StatisticsHolder statisticsHolder, final Map<Class<? extends Configuration>,
      Configuration> configurations, final List<AssertionEvaluator> assertions) throws TestException {

    CacheConfig<K, V> cacheConfig = (CacheConfig<K, V>)configurations.get(CacheConfig.class);
    int bulkBatchSize = cacheConfig.getBulkBatchSize();
    final long next = this.sequenceGenerator.next();
    Set<K> set = Collections.newSetFromMap(new WeakHashMap<K, Boolean>());
    for (int i = 0; i < bulkBatchSize; i++) {
      set.add(keyGenerator.generate(next));
    }
    List<Cache<K, V>> caches = cacheConfig.getCaches();
    for (final Cache<K, V> cache : caches) {
      long start = getTimeInNs();
      try {
        cache.removeAll(set);
        long end = getTimeInNs();
        statisticsHolder.record(cacheConfig.getCacheName(cache), (end - start), REMOVEALL);
      } catch (Exception e) {
        long end = getTimeInNs();
        statisticsHolder.record(cacheConfig.getCacheName(cache), (end - start), EXCEPTION);
      }
    }
  }

  @Override
  public List<String> getDescription() {
    List<String> desc = new ArrayList<String>();
    desc.add("removeAll(Set<? extends " + keyGenerator.getDescription() + "> keys)");
    desc.add(sequenceGenerator.getDescription());
    return desc;
  }

}
