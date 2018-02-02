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

package io.rainfall.ehcache3;

import io.rainfall.ObjectGenerator;
import io.rainfall.Operation;
import io.rainfall.SequenceGenerator;
/**
 * Contains the helper methods to instantiate the Ehcache {@link io.rainfall.Operation} objects.
 *
 * @author Aurelien Broszniowski
 */
public class Ehcache3Operations {

  @Deprecated
  public static <K, V> io.rainfall.deprecated.ehcache3.operation.PutOperation<K, V> put(Class<K> keyClass, Class<V> valueClass) {
    return new io.rainfall.deprecated.ehcache3.operation.PutOperation<K, V>();
  }

  public static <K, V> Operation put(final ObjectGenerator<K> keyGenerator, final ObjectGenerator<V> valueGenerator,
                                     final SequenceGenerator sequenceGenerator, final Iterable<CacheDefinition<K, V>> cacheDefinitions) {
    return new io.rainfall.ehcache3.operation.PutOperation<K, V>(keyGenerator, valueGenerator, sequenceGenerator, cacheDefinitions);
  }

  public static <K, V> Operation put(final ObjectGenerator<K> keyGenerator, final ObjectGenerator<V> valueGenerator,
                                     final SequenceGenerator sequenceGenerator, final long tpsLimit, final Iterable<CacheDefinition<K, V>> cacheDefinition) {
    return new io.rainfall.ehcache3.operation.TpsLimitPutOperation<K, V>(keyGenerator, valueGenerator, sequenceGenerator, tpsLimit, cacheDefinition);
  }

  public static <K, V> Operation put(final ObjectGenerator<K> keyGenerator, final ObjectGenerator<V> valueGenerator,
                                     final SequenceGenerator sequenceGenerator, final boolean verified, final Iterable<CacheDefinition<K, V>> cacheDefinition) {
    return new io.rainfall.ehcache3.operation.PutVerifiedOperation<K, V>(keyGenerator, valueGenerator, sequenceGenerator, cacheDefinition);
  }

  @Deprecated
  public static <K, V> io.rainfall.deprecated.ehcache3.operation.GetOperation<K, V> get(Class<K> keyClass, Class<V> valueClass) {
    return new io.rainfall.deprecated.ehcache3.operation.GetOperation<K, V>();
  }

  public static <K, V> Operation get(final ObjectGenerator<K> keyGenerator,
                                     final SequenceGenerator sequenceGenerator, final Iterable<CacheDefinition<K, V>> cacheDefinitions) {
    return new io.rainfall.ehcache3.operation.GetOperation<K, V>(keyGenerator, sequenceGenerator, cacheDefinitions);
  }

  @Deprecated
  public static <K, V> io.rainfall.deprecated.ehcache3.operation.GetOperation<K, V> get(Class<K> keyClass, Class<V> valueClass, long tpsLimit) {
    return new io.rainfall.deprecated.ehcache3.operation.TpsLimitGetOperation<K, V>(tpsLimit);
  }

  @Deprecated
  public static <K, V> io.rainfall.deprecated.ehcache3.operation.RemoveOperation<K, V> remove(Class<K> keyClass, Class<V> valueClass) {
    return new io.rainfall.deprecated.ehcache3.operation.RemoveOperation<K, V>();
  }

  @Deprecated
  public static <K, V> io.rainfall.deprecated.ehcache3.operation.PutAllOperation<K, V> putAll(Class<K> keyClass, Class<V> valueClass) {
    return new io.rainfall.deprecated.ehcache3.operation.PutAllOperation<K, V>();
  }

  @Deprecated
  public static <K, V> io.rainfall.deprecated.ehcache3.operation.GetAllOperation<K, V> getAll(Class<K> keyClass, Class<V> valueClass) {
    return new io.rainfall.deprecated.ehcache3.operation.GetAllOperation<K, V>();
  }

  @Deprecated
  public static <K, V> io.rainfall.deprecated.ehcache3.operation.RemoveAllOperation<K, V> removeAll(Class<K> keyClass, Class<V> valueClass) {
    return new io.rainfall.deprecated.ehcache3.operation.RemoveAllOperation<K, V>();
  }

  @Deprecated
  public static <K, V> io.rainfall.deprecated.ehcache3.operation.PutIfAbsentOperation<K, V> putIfAbsent(Class<K> keyClass, Class<V> valueClass) {
    return new io.rainfall.deprecated.ehcache3.operation.PutIfAbsentOperation<K, V>();
  }

  @Deprecated
  public static <K, V> io.rainfall.deprecated.ehcache3.operation.ReplaceOperation<K, V> replace(Class<K> keyClass, Class<V> valueClass) {
    return new io.rainfall.deprecated.ehcache3.operation.ReplaceOperation<K, V>();
  }

  @Deprecated
  public static <K, V> io.rainfall.deprecated.ehcache3.operation.ReplaceForKeyAndValueOperation<K, V> replaceForKeyAndValue(Class<K> keyClass, Class<V> valueClass) {
    return new io.rainfall.deprecated.ehcache3.operation.ReplaceForKeyAndValueOperation<K, V>();
  }

  @Deprecated
  public static <K, V> io.rainfall.deprecated.ehcache3.operation.RemoveForKeyAndValueOperation<K, V> removeForKeyAndValue(Class<K> keyClass, Class<V> valueClass) {
    return new io.rainfall.deprecated.ehcache3.operation.RemoveForKeyAndValueOperation<K, V>();
  }

}
