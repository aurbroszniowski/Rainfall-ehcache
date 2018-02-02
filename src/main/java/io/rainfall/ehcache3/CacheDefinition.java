package io.rainfall.ehcache3;

import org.ehcache.Cache;

/**
 * @author Aurelien Broszniowski
 */

public class CacheDefinition<K, V> {

  private String name;
  private Cache<K, V> cache;

  public CacheDefinition(final String name, final Cache<K, V> cache) {
    this.name = name;
    this.cache = cache;
  }

  public static <X, Y> CacheDefinition<X, Y> cache(String name, Cache<X, Y> cache) {
    return new CacheDefinition<X, Y>(name, cache);
  }

  public String getName() {
    return name;
  }

  public Cache<K, V> getCache() {
    return cache;
  }
}
