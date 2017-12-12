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

package io.rainfall.ehcache2;

import io.rainfall.ObjectGenerator;
import io.rainfall.SequenceGenerator;
import io.rainfall.ehcache2.operation.GetOperation;
import io.rainfall.ehcache2.operation.PutIfAbsentOperation;
import io.rainfall.ehcache2.operation.PutOperation;
import io.rainfall.ehcache2.operation.RemoveOperation;

/**
 * Contains the helper methods to instantiate the Ehcache {@link io.rainfall.Operation} objects.
 *
 * @author Aurelien Broszniowski
 */
public class Ehcache2Operations {

  public static <K, V> PutOperation<K, V> put(final ObjectGenerator<K> keyGenerator, final ObjectGenerator<V> valueGenerator,
                                              final SequenceGenerator sequenceGenerator, final CacheDefinition cacheDefinition) {
    return new PutOperation<K, V>(keyGenerator, valueGenerator, sequenceGenerator, cacheDefinition);
  }

  public static <K, V> PutOperation<K, V> put(final ObjectGenerator<K> keyGenerator, final ObjectGenerator<V> valueGenerator,
                                              final SequenceGenerator sequenceGenerator, final CacheDefinition... cacheDefinitions) {
    return new PutOperation<K, V>(keyGenerator, valueGenerator, sequenceGenerator, cacheDefinitions);
  }

  public static <K, V> GetOperation<K, V> get(Class<K> keyClass, Class<V> valueClass) {
    return new GetOperation<K, V>();
  }

  public static <K, V> RemoveOperation<K, V> remove(Class<K> keyClass, Class<V> valueClass) {
    return new RemoveOperation<K, V>();
  }

  public static <K, V> PutIfAbsentOperation<K, V> putIfAbsent(Class<K> keyClass, Class<V> valueClass) {
    return new PutIfAbsentOperation<K, V>();
  }


}
