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
import io.rainfall.Operation;
import io.rainfall.SequenceGenerator;
import io.rainfall.deprecated.ehcache2.operation.GetOperation;
import io.rainfall.deprecated.ehcache2.operation.PutIfAbsentOperation;
import io.rainfall.deprecated.ehcache2.operation.PutOperation;
import io.rainfall.deprecated.ehcache2.operation.RemoveOperation;

/**
 * Contains the helper methods to instantiate the Ehcache {@link io.rainfall.Operation} objects.
 *
 * @author Aurelien Broszniowski
 */
public class Ehcache2Operations {

  @Deprecated
  public static <K, V> PutOperation<K, V> put(Class<K> keyClass, Class<V> valueClass) {
    return new io.rainfall.deprecated.ehcache2.operation.PutOperation<K, V>();
  }

  public static <K, V> Operation put(final ObjectGenerator<K> keyGenerator, final ObjectGenerator<V> valueGenerator,
                                     final SequenceGenerator sequenceGenerator, final Iterable<CacheDefinition> cacheDefinitions) {
    return new io.rainfall.ehcache2.operation.PutOperation<K, V>(keyGenerator, valueGenerator, sequenceGenerator, cacheDefinitions);
  }

  @Deprecated
  public static <K, V> GetOperation<K, V> get(Class<K> keyClass, Class<V> valueClass) {
    return new io.rainfall.deprecated.ehcache2.operation.GetOperation<K, V>();
  }

  public static <K, V> Operation get(final ObjectGenerator<K> keyGenerator,
                                              final SequenceGenerator sequenceGenerator, final Iterable<CacheDefinition> cacheDefinitions) {
    return new io.rainfall.ehcache2.operation.GetOperation<K, V>(keyGenerator, sequenceGenerator, cacheDefinitions);
  }

  @Deprecated
  public static <K, V> RemoveOperation<K, V> remove(Class<K> keyClass, Class<V> valueClass) {
    return new io.rainfall.deprecated.ehcache2.operation.RemoveOperation<K, V>();
  }

  public static <K, V> Operation remove(final ObjectGenerator<K> keyGenerator,
                                     final SequenceGenerator sequenceGenerator, final Iterable<CacheDefinition> cacheDefinitions) {
    return new io.rainfall.ehcache2.operation.RemoveOperation<K, V>(keyGenerator, sequenceGenerator, cacheDefinitions);
  }

  @Deprecated
  public static <K, V> PutIfAbsentOperation<K, V> putIfAbsent(Class<K> keyClass, Class<V> valueClass) {
    return new io.rainfall.deprecated.ehcache2.operation.PutIfAbsentOperation<K, V>();
  }

  public static <K, V> Operation putIfAbsent(final ObjectGenerator<K> keyGenerator, final ObjectGenerator<V> valueGenerator,
                                     final SequenceGenerator sequenceGenerator, final Iterable<CacheDefinition> cacheDefinitions) {
    return new io.rainfall.ehcache2.operation.PutIfAbsentOperation<K, V>(keyGenerator, valueGenerator, sequenceGenerator, cacheDefinitions);
  }


}
