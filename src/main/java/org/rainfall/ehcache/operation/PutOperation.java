package org.rainfall.ehcache.operation;

import org.rainfall.AssertionEvaluator;
import org.rainfall.Configuration;
import org.rainfall.Operation;
import org.rainfall.TestException;
import org.rainfall.statistics.StatisticsObserversFactory;

import java.util.List;
import java.util.Map;

/**
 * @author Aurelien Broszniowski
 */
public class PutOperation<K, V> extends Operation {

  @Override
  public void exec(final StatisticsObserversFactory statisticsObserversFactory, final Map<Class<? extends Configuration>,
      Configuration> configurations, final List<AssertionEvaluator> assertions) throws TestException {

  }
}
