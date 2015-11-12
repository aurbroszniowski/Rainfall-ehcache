package io.rainfall.ehcache.statistics;

/**
 * @author Aurelien Broszniowski
 */
public enum EhcacheResult {
  PUT, GET, MISS, REMOVE, REMOVEVALUE, EXCEPTION, PUTALL, GETALL, REMOVEALL, PUTIFABSENT, REPLACE, REPLACEVALUE,
  REPLACE_MISS, PUTIFABSENT_MISS, REPLACEVALUE_MISS
}
