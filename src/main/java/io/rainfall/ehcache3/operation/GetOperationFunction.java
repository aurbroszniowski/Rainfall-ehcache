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

import static io.rainfall.ehcache.statistics.EhcacheResult.EXCEPTION;
import static io.rainfall.ehcache.statistics.EhcacheResult.GET;
import static io.rainfall.ehcache.statistics.EhcacheResult.MISS;

/**
 * Pure function to execute a Ehcache get
 *
 * @author Aurelien Broszniowski
 */
public class GetOperationFunction<K, V> extends OperationFunction<EhcacheResult> {

  private Cache<K, V> cache;
  private long next;
  private ObjectGenerator<K> keyGenerator;

  public FunctionExecutor execute(final Cache<K, V> cache, final long next, final ObjectGenerator<K> keyGenerator) {
    this.cache = cache;
    this.next = next;
    this.keyGenerator = keyGenerator;
    return this.functionExecutor;
  }

  @Override
  public EhcacheResult apply() throws Exception {
    V value;
    try {
      value = cache.get(keyGenerator.generate(next));
    } catch (Exception e) {
      return EXCEPTION;
    }
    if (value == null) {
      return MISS;
    } else {
      return GET;
    }
  }
}
