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

import io.rainfall.ehcache3.operation.GetAllOperation;
import io.rainfall.ehcache3.operation.GetOperation;
import io.rainfall.ehcache3.operation.PutAllOperation;
import io.rainfall.ehcache3.operation.PutIfAbsentOperation;
import io.rainfall.ehcache3.operation.PutOperation;
import io.rainfall.ehcache3.operation.RemoveAllOperation;
import io.rainfall.ehcache3.operation.RemoveForKeyAndValueOperation;
import io.rainfall.ehcache3.operation.RemoveOperation;
import io.rainfall.ehcache3.operation.ReplaceOperation;
import io.rainfall.ehcache3.operation.ReplaceForKeyAndValueOperation;

/**
 * Contains the helper methods to instantiate the Ehcache {@link io.rainfall.Operation} objects.
 *
 * @author Aurelien Broszniowski
 */
public class Ehcache3Operations {

  public static <K, V> PutOperation<K, V> put(Class<K> keyClass, Class<V> valueClass) {
    return new PutOperation<K, V>();
  }

  public static <K, V> GetOperation<K, V> get(Class<K> keyClass, Class<V> valueClass) {
    return new GetOperation<K, V>();
  }

  public static <K, V> RemoveOperation<K, V> remove(Class<K> keyClass, Class<V> valueClass) {
    return new RemoveOperation<K, V>();
  }

  public static <K, V> PutAllOperation<K, V> putAll(Class<K> keyClass, Class<V> valueClass) {
    return new PutAllOperation<K, V>();
  }

  public static <K, V> GetAllOperation<K, V> getAll(Class<K> keyClass, Class<V> valueClass) {
    return new GetAllOperation<K, V>();
  }

  public static <K, V> RemoveAllOperation<K, V> removeAll(Class<K> keyClass, Class<V> valueClass) {
    return new RemoveAllOperation<K, V>();
  }

  public static <K, V> PutIfAbsentOperation<K, V> putIfAbsent(Class<K> keyClass, Class<V> valueClass) {
    return new PutIfAbsentOperation<K, V>();
  }

  public static <K, V> ReplaceOperation<K, V> replace(Class<K> keyClass, Class<V> valueClass) {
    return new ReplaceOperation<K, V>();
  }

  public static <K, V> ReplaceForKeyAndValueOperation<K, V> replaceForKeyAndValue(Class<K> keyClass, Class<V> valueClass) {
    return new ReplaceForKeyAndValueOperation<K, V>();
  }

  public static <K, V> RemoveForKeyAndValueOperation<K, V> removeForKeyAndValue(Class<K> keyClass, Class<V> valueClass) {
    return new RemoveForKeyAndValueOperation<K, V>();
  }

}
