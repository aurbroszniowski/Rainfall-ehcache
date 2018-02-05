/*
 * Copyright (c) 2014-2018 Aurélien Broszniowski
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

import net.sf.ehcache.Ehcache;

/**
 * @author Aurelien Broszniowski
 */

public class CacheDefinition {

  private String name;
  private Ehcache cache;

  public CacheDefinition(final String name, final Ehcache cache) {
    this.name = name;
    this.cache = cache;
  }

  public static CacheDefinition cache(String name, Ehcache cache) {
    return new CacheDefinition(name, cache);
  }

  public String getName() {
    return name;
  }

  public Ehcache getCache() {
    return cache;
  }
}
