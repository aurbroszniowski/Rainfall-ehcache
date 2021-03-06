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

import io.rainfall.Configuration;
import net.sf.ehcache.Ehcache;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Aurelien Broszniowski
 */

@Deprecated
public class CacheConfig<K, V> extends Configuration {

  private List<Ehcache> caches = new ArrayList<Ehcache>();
  private Map<Ehcache, String> cacheNames = new HashMap<Ehcache, String>();

  public static <K, V> CacheConfig<K, V> cacheConfig() {
    return new CacheConfig<K, V>();
  }

  public List<Ehcache> getCaches() {
    return caches;
  }

  public CacheConfig<K, V> cache(final String cacheName, final Ehcache cache) {
    this.caches.add(cache);
    this.cacheNames.put(cache, cacheName);
    return this;
  }

  public CacheConfig<K, V> caches(final List<Ehcache> caches) {
    for (Ehcache cache : caches) {
      this.caches.add(cache);
      this.cacheNames.put(cache, cache.getName());
    }
    return this;
  }

  public CacheConfig<K, V> caches(final Ehcache... caches) {
    for (Ehcache cache : caches) {
      this.caches.add(cache);
      this.cacheNames.put(cache, cache.getName());
    }
    return this;
  }

  public String getCacheName(Ehcache cache) {
    return cacheNames.get(cache);
  }

  @Override
  public List<String> getDescription() {
    return Arrays.asList("Using " + caches.size() + " cache" + (caches.size() > 1 ? "s" : ""));
  }
}
