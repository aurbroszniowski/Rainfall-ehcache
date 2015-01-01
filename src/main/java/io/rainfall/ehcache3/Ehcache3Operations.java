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

import io.rainfall.ehcache3.operation.GetOperation;
import io.rainfall.ehcache3.operation.PutOperation;
import io.rainfall.ehcache3.operation.RemoveOperation;

/**
 * Contains the helper methods to instantiate the Ehcache {@link io.rainfall.Operation} objects.
 *
 * @author Aurelien Broszniowski
 */
public class Ehcache3Operations {

  public static <K, V> PutOperation<K, V> put() {
    return new PutOperation<K, V>();
  }

  public static <K, V> GetOperation<K, V> get() {
    return new GetOperation<K, V>();
  }

  public static <K, V> RemoveOperation<K, V> remove() {
    return new RemoveOperation<K, V>();
  }

}
