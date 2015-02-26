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

import io.rainfall.Configuration;
import org.ehcache.Cache;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Aurelien Broszniowski
 */

public class CacheConfig<K, V> extends Configuration {

  private List<Cache<K, V>> caches = new ArrayList<Cache<K, V>>();

  public static <K, V> CacheConfig<K, V> cacheConfig(Class<K> keyClass, final Class<V> valueClass) {
    return new CacheConfig<K, V>();
  }

  public List<Cache<K, V>> getCaches() {
    return caches;
  }

  /**
   * This builder pattern has been implemented in order to avoid the warning of using generics with varargs
   *
   * @param cache
   * @return this config
   */

  public CacheConfig<K, V> cache(final Cache<K, V> cache) {
    caches.add(cache);
    return this;
  }

  public CacheConfig<K, V> and(final Cache<K, V> cache) {
    caches.add(cache);
    return this;
  }
}
