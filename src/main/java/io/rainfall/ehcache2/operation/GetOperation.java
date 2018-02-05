/*
 * Copyright (c) 2014-2018 Aurélien Broszniowski
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
import io.rainfall.ObjectGenerator;
import io.rainfall.Operation;
import io.rainfall.SequenceGenerator;
import io.rainfall.TestException;
import io.rainfall.ehcache2.CacheDefinition;
import io.rainfall.statistics.StatisticsHolder;
import net.sf.ehcache.Ehcache;
import net.sf.ehcache.Element;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static io.rainfall.ehcache.statistics.EhcacheResult.EXCEPTION;
import static io.rainfall.ehcache.statistics.EhcacheResult.GET;
import static io.rainfall.ehcache.statistics.EhcacheResult.MISS;

/**
 * Execute and measure a Ehcache get operation
 *
 * @author Aurelien Broszniowski
 */
public class GetOperation<K, V>  implements Operation {

  private final ObjectGenerator<K> keyGenerator;
  private final SequenceGenerator sequenceGenerator;
  private final Iterable<CacheDefinition> cacheDefinitions;

  public GetOperation(final ObjectGenerator<K> keyGenerator, final SequenceGenerator sequenceGenerator,
                      final Iterable<CacheDefinition> cacheDefinitions) {
    this.keyGenerator = keyGenerator;
    this.sequenceGenerator = sequenceGenerator;
    this.cacheDefinitions = cacheDefinitions;
  }

  @Override
  public void exec(final StatisticsHolder statisticsHolder, final Map<Class<? extends Configuration>,
      Configuration> configurations, final List<AssertionEvaluator> assertions) throws TestException {

    final long next = this.sequenceGenerator.next();
    for (final CacheDefinition cacheDefinition : cacheDefinitions) {
      Ehcache cache = cacheDefinition.getCache();
      Element value;
      Object k = keyGenerator.generate(next);

      long start = statisticsHolder.getTimeInNs();
      try {
        value = cache.get(k);
        long end = statisticsHolder.getTimeInNs();
        if (value == null) {
          statisticsHolder.record(cacheDefinition.getName(), (end - start), MISS);
        } else {
          statisticsHolder.record(cacheDefinition.getName(), (end - start), GET);
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
    desc.add("get(" + keyGenerator.getDescription() + " key)");
    desc.add(sequenceGenerator.getDescription());
    return desc;
  }


}
