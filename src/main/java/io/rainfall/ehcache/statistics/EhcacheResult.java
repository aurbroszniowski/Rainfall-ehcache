package io.rainfall.ehcache.statistics;

import io.rainfall.statistics.Result;

/**
 * @author Aurelien Broszniowski
 */
public class EhcacheResult extends Result {
  public static Result PUT = new Result("PUT");
  public static Result GET = new Result("GET");
  public static Result MISS = new Result("MISS");
  public static Result REMOVE = new Result("REMOVE");
  public static Result EXCEPTION = new Result("EXCEPTION");

  public EhcacheResult(final String result) {
    super(result);
  }

  public static Result[] values() {
    return new Result[] { PUT, GET, MISS, REMOVE, EXCEPTION };
  }

}
