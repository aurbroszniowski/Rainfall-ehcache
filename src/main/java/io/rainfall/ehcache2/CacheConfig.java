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
import java.util.Collections;
import java.util.List;

/**
 * @author Aurelien Broszniowski
 */

public class CacheConfig<K, V> extends Configuration {

  private List<Ehcache> caches = new ArrayList<Ehcache>();

  public static <K, V> CacheConfig<K, V> cacheConfig() {
    return new CacheConfig<K, V>();
  }

  public CacheConfig<K, V> caches(final Ehcache... caches) {
    Collections.addAll(this.caches, caches);
    return this;
  }

  public List<Ehcache> getCaches() {
    return caches;
  }

  @Override
  public List<String> getDescription() {
    return Arrays.asList("Using " + caches.size() + " cache" + (caches.size() > 1 ? "s" : ""));
  }
}
