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
import io.rainfall.ObjectGenerator;
import io.rainfall.SequenceGenerator;
import io.rainfall.generator.IterationSequenceGenerator;
import io.rainfall.generator.RandomSequenceGenerator;
import io.rainfall.generator.sequence.Distribution;
import io.rainfall.utils.ConcurrentPseudoRandom;
import net.sf.ehcache.Ehcache;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


/**
 * @author Aurelien Broszniowski
 */

public class CacheConfig<K, V> extends Configuration {

  private List<Ehcache> caches = new ArrayList<Ehcache>();
  private ObjectGenerator<K> keyGenerator = null;
  private ObjectGenerator<V> valueGenerator = null;
  private SequenceGenerator sequenceGenerator = null;
  private ConcurrentPseudoRandom randomizer = new ConcurrentPseudoRandom();

  public static <K, V> CacheConfig<K, V> cacheConfig() {
    return new CacheConfig<K, V>();
  }

  public CacheConfig<K, V> caches(final Ehcache... caches) {
    Collections.addAll(this.caches, caches);
    return this;
  }

  public CacheConfig<K, V> using(final ObjectGenerator<K> keyGenerator, final ObjectGenerator<V> valueGenerator) {
    if (this.keyGenerator != null) {
      throw new IllegalStateException("KeyGenerator already chosen.");
    }
    this.keyGenerator = keyGenerator;

    if (this.valueGenerator != null) {
      throw new IllegalStateException("ValueGenerator already chosen.");
    }
    this.valueGenerator = valueGenerator;
    return this;
  }

  public CacheConfig<K, V> sequentially() {
    if (this.sequenceGenerator != null) {
      throw new IllegalStateException("SequenceGenerator already chosen.");
    }
    this.sequenceGenerator = new IterationSequenceGenerator();
    return this;
  }

  public CacheConfig<K, V> atRandom(Distribution distribution, long min, long max, long width) {
    if (sequenceGenerator == null) {
      this.sequenceGenerator = new RandomSequenceGenerator(distribution, min, max, width);
    } else {
      throw new IllegalStateException("SequenceGenerator already chosen");
    }
    return this;
  }

  public List<Ehcache> getCaches() {
    return caches;
  }

  public ObjectGenerator<K> getKeyGenerator() {
    return keyGenerator;
  }

  public ObjectGenerator<V> getValueGenerator() {
    return valueGenerator;
  }

  public SequenceGenerator getSequenceGenerator() {
    return sequenceGenerator;
  }

  public ConcurrentPseudoRandom getRandomizer() {
    return randomizer;
  }
}
