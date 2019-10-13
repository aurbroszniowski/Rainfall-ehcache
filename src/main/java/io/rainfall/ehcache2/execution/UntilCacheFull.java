/*
 * Copyright (c) 2014-2019 Aurélien Broszniowski
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

package io.rainfall.ehcache2.execution;

import io.rainfall.AssertionEvaluator;
import io.rainfall.Configuration;
import io.rainfall.Execution;
import io.rainfall.Scenario;
import io.rainfall.TestException;
import io.rainfall.WeightedOperation;
import io.rainfall.configuration.ConcurrencyConfig;
import io.rainfall.ehcache2.CacheConfig;
import io.rainfall.statistics.StatisticsHolder;
import io.rainfall.utils.RangeMap;
import net.sf.ehcache.Ehcache;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;

import static java.util.concurrent.TimeUnit.SECONDS;

/**
 * @author Aurelien Broszniowski
 */
public class UntilCacheFull extends Execution {
  @Override
  public <E extends Enum<E>> void execute(final StatisticsHolder<E> statisticsHolder, final Scenario scenario, final Map<Class<? extends Configuration>, Configuration> configurations, final List<AssertionEvaluator> assertions) throws TestException {

    ConcurrencyConfig concurrencyConfig = (ConcurrencyConfig)configurations.get(ConcurrencyConfig.class);

    markExecutionState(scenario, ExecutionState.BEGINNING);

    final Map<String, ExecutorService> executors = concurrencyConfig.createFixedExecutorService();
    for (final String threadpoolName : executors.keySet()) {
      final int threadCount = concurrencyConfig.getThreadCount(threadpoolName);
      final ExecutorService executor = executors.get(threadpoolName);

      CacheConfig cachesConfig = (CacheConfig)configurations.get(CacheConfig.class);
      final List<Ehcache> caches = cachesConfig.getCaches();
      final Map<String, Integer> sizes = new HashMap<String, Integer>();
      for (Ehcache cache : caches) {
        sizes.put(cache.getName(), Integer.MIN_VALUE);
      }

      for (int threadNb = 0; threadNb < threadCount; threadNb++) {
        executor.submit(new Callable() {

          @Override
          public Object call() throws Exception {
            Thread.currentThread().setName("Rainfall-core Operations Thread");
            RangeMap<WeightedOperation> operations = scenario.getOperations().get(threadpoolName);

            while (!cachesAreFull(sizes, caches)) {
              operations.getNextRandom(weightRnd)
                  .getOperation().exec(statisticsHolder, configurations, assertions);
            }
            return null;
          }
        });
      }
    }
    //TODO : it is submitted enough but not everything has finished to run when threads are done -> how to solve Coordinated Omission ?

    markExecutionState(scenario, ExecutionState.ENDING);
    for (ExecutorService executor : executors.values()) {
      executor.shutdown();
    }
    try {
      boolean success = true;
      for (ExecutorService executor : executors.values()) {
        boolean executorSuccess = executor.awaitTermination(60, SECONDS);
        if (!executorSuccess) {
          executor.shutdownNow();
          success &= executor.awaitTermination(60, SECONDS);
        }
      }

      if (!success) {
        throw new TestException("Execution of Scenario timed out.");
      }
    } catch (InterruptedException e) {
      throw new TestException("Execution of Scenario didn't stop correctly.", e);
    }
  }

  @Override
  public String getDescription() {
    return "Execution : until caches are full";
  }


  private boolean cachesAreFull(final Map<String, Integer> sizes, final List<Ehcache> caches) {
    boolean allCachesAreFull = true;
    for (Ehcache cache : caches) {
      allCachesAreFull &= (cache.getStatistics().getSize() == sizes.get(cache.getName()));
      sizes.put(cache.getName(), cache.getSize());
    }
    return allCachesAreFull;
  }
}

