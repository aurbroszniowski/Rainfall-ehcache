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

import io.rainfall.ObjectGenerator;
import io.rainfall.ehcache.statistics.EhcacheResult;
import io.rainfall.statistics.FunctionExecutor;
import io.rainfall.statistics.OperationFunction;
import org.ehcache.Cache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;
import java.util.WeakHashMap;

import static io.rainfall.ehcache.statistics.EhcacheResult.EXCEPTION;
import static io.rainfall.ehcache.statistics.EhcacheResult.PUTALL;

/**
 * Pure function to execute a Ehcache putAll
 *
 * @author Aurelien Broszniowski
 */
public class PutAllOperationFunction<K, V> extends OperationFunction<EhcacheResult> {

  private static final Logger log = LoggerFactory.getLogger(PutAllOperationFunction.class);

  private Cache<K, V> cache;
  private long next;
  private ObjectGenerator<K> keyGenerator;
  private ObjectGenerator<V> valueGenerator;
  private int bulkBatchSize;

  public FunctionExecutor execute(final Cache<K, V> cache, final long next,
                                  final ObjectGenerator<K> keyGenerator, final ObjectGenerator<V> valueGenerator,
                                  int bulkBatchSize) {
    this.cache = cache;
    this.next = next;
    this.keyGenerator = keyGenerator;
    this.valueGenerator = valueGenerator;
    this.bulkBatchSize = bulkBatchSize;
    return this.functionExecutor;
  }

  @Override
  public EhcacheResult apply() throws Exception {
    try {
      Map<K, V> maps = new WeakHashMap<K, V>();
      for (int i = 0; i < bulkBatchSize; i++) {
        maps.put(keyGenerator.generate(next), valueGenerator.generate(next));
      }
      cache.putAll(maps);
    } catch (Exception e) {
      log.debug("putAll operation failed.", e);
      return EXCEPTION;
    }
    return PUTALL;
  }
}
