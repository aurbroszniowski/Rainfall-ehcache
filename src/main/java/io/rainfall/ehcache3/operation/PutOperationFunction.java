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

import static io.rainfall.ehcache.statistics.EhcacheResult.EXCEPTION;
import static io.rainfall.ehcache.statistics.EhcacheResult.PUT;

/**
 * Pure function to execute a Ehcache put
 *
 * @author Aurelien Broszniowski
 */
public class PutOperationFunction<K, V> extends OperationFunction<EhcacheResult> {

  private static final Logger log = LoggerFactory.getLogger(PutOperationFunction.class);

  private Cache<K, V> cache;
  private long next;
  private ObjectGenerator<K> keyGenerator;
  private ObjectGenerator<V> valueGenerator;

  public FunctionExecutor execute(final Cache<K, V> cache, final long next,
                                  final ObjectGenerator<K> keyGenerator, final ObjectGenerator<V> valueGenerator) {
    this.cache = cache;
    this.next = next;
    this.keyGenerator = keyGenerator;
    this.valueGenerator = valueGenerator;
    return functionExecutor;
  }

  @Override
  public EhcacheResult apply() throws Exception {
    try {
      cache.put(keyGenerator.generate(next), valueGenerator.generate(next));
    } catch (Exception e) {
      log.debug("put operation failed.", e);
      return EXCEPTION;
    }
    return PUT;
  }
}
