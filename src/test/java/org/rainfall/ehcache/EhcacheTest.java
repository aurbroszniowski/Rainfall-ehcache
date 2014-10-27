package org.rainfall.ehcache;

import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Ehcache;
import net.sf.ehcache.config.CacheConfiguration;
import net.sf.ehcache.config.Configuration;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.rainfall.Runner;
import org.rainfall.Scenario;
import org.rainfall.SyntaxException;
import org.rainfall.configuration.ConcurrencyConfig;
import org.rainfall.configuration.ReportingConfig;
import org.rainfall.generator.ByteArrayGenerator;
import org.rainfall.generator.StringGenerator;
import org.rainfall.utils.SystemTest;

import static java.util.concurrent.TimeUnit.MINUTES;
import static org.rainfall.ehcache.EhcacheOperations.get;
import static org.rainfall.ehcache.EhcacheOperations.put;
import static org.rainfall.ehcache.EhcacheOperations.remove;
import static org.rainfall.ehcache.operation.OperationWeight.OPERATION.GET;
import static org.rainfall.ehcache.operation.OperationWeight.OPERATION.PUT;
import static org.rainfall.ehcache.operation.OperationWeight.OPERATION.REMOVE;
import static org.rainfall.ehcache.operation.OperationWeight.operation;
import static org.rainfall.execution.Executions.nothingFor;
import static org.rainfall.execution.Executions.times;
import static org.rainfall.unit.TimeDivision.seconds;

/**
 * @author Aurelien Broszniowski
 */

@Category(SystemTest.class)
public class EhcacheTest {

  @Test
  public void testLoad() throws SyntaxException {
    Configuration configuration = new Configuration().name("EhcacheTest")
        .defaultCache(new CacheConfiguration("default", 0))
        .cache(new CacheConfiguration("one", 0));
    CacheManager cacheManager = CacheManager.create(configuration);
    Ehcache cache = cacheManager.getEhcache("one");

    CacheConfig<String, Byte[]> cacheConfig = CacheConfig.<String, Byte[]>cacheConfig()
        .caches(cache)
        .using(StringGenerator.fixedLength(10), ByteArrayGenerator.fixedLength(128))
        .sequentially()
        .weights(operation(PUT, 0.10), operation(GET, 0.80), operation(REMOVE, 0.10));
    ConcurrencyConfig concurrency = ConcurrencyConfig.concurrencyConfig()
        .threads(4).timeout(5, MINUTES);
    ReportingConfig reporting = ReportingConfig.reportingConfig(ReportingConfig.text(), ReportingConfig.html());

    Scenario scenario = Scenario.scenario("Cache load")
        .exec(put())
        .exec(get())
        .exec(remove());

    Runner.setUp(scenario)
        .executed(times(10000000), nothingFor(10, seconds))
        .config(cacheConfig, concurrency, reporting)
//          .assertion(latencyTime(), isLessThan(1, seconds))
        .start();

    cacheManager.shutdown();
  }
}
