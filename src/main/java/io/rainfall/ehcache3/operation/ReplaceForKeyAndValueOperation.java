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
import io.rainfall.ObjectGenerator;
import io.rainfall.Operation;
import io.rainfall.SequenceGenerator;
import io.rainfall.TestException;
import io.rainfall.ehcache3.CacheDefinition;
import io.rainfall.statistics.StatisticsHolder;
import org.ehcache.Cache;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static io.rainfall.ehcache.statistics.EhcacheResult.EXCEPTION;
import static io.rainfall.ehcache.statistics.EhcacheResult.REPLACEVALUE;
import static io.rainfall.ehcache.statistics.EhcacheResult.REPLACEVALUE_MISS;

/**
 * @author Aurelien Broszniowski
 */
public class ReplaceForKeyAndValueOperation<K, V> implements Operation {

  private final ObjectGenerator<K> keyGenerator;
  private final ObjectGenerator<V> valueGenerator;
  private final SequenceGenerator sequenceGenerator;
  private final Iterable<CacheDefinition<K, V>> cacheDefinitions;

  public ReplaceForKeyAndValueOperation(final ObjectGenerator<K> keyGenerator, final ObjectGenerator<V> valueGenerator,
                                        final SequenceGenerator sequenceGenerator, final Iterable<CacheDefinition<K, V>> cacheDefinitions) {
    this.keyGenerator = keyGenerator;
    this.valueGenerator = valueGenerator;
    this.sequenceGenerator = sequenceGenerator;
    this.cacheDefinitions = cacheDefinitions;
  }

  @Override
  public void exec(final StatisticsHolder statisticsHolder, final Map<Class<? extends Configuration>,
      Configuration> configurations, final List<AssertionEvaluator> assertions) throws TestException {

    final long next = this.sequenceGenerator.next();
    for (final CacheDefinition<K, V> cacheDefinition : cacheDefinitions) {
      Cache<K, V> cache = cacheDefinition.getCache();
      boolean replaced;
      K k = keyGenerator.generate(next);
      V v = valueGenerator.generate(next + 1);

      long start = statisticsHolder.getTimeInNs();
      try {
        replaced = cache.replace(k, v, v);
        long end = statisticsHolder.getTimeInNs();
        if (!replaced) {
          statisticsHolder.record(cacheDefinition.getName(), (end - start), REPLACEVALUE_MISS);
        } else {
          statisticsHolder.record(cacheDefinition.getName(), (end - start), REPLACEVALUE);
        }
      } catch (Exception e) {
        long end = statisticsHolder.getTimeInNs();
        statisticsHolder.record(cacheDefinition.getName(), (end - start), EXCEPTION);
      }
    }
  }

  @Override
  public List<String> getDescription() {
    List<String> desc = new ArrayList<String>();
    desc.add("replace(" + keyGenerator.getDescription() + " key, " + valueGenerator.getDescription() + " oldValue, " + valueGenerator
        .getDescription() + " newValue)");
    desc.add(sequenceGenerator.getDescription());
    return desc;
  }
}
