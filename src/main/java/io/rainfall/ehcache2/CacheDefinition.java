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
