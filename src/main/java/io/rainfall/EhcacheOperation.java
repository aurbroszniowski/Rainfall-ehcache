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

package io.rainfall;

import io.rainfall.generator.IterationSequenceGenerator;
import io.rainfall.generator.RandomSequenceGenerator;
import io.rainfall.generator.sequence.Distribution;
import io.rainfall.utils.NullObjectGenerator;
import io.rainfall.utils.NullSequenceGenerator;

/**
 * @author Aurelien Broszniowski
 */

public abstract class EhcacheOperation<K, V> extends Operation {

  protected ObjectGenerator<K> keyGenerator = NullObjectGenerator.instance();
  protected ObjectGenerator<V> valueGenerator = NullObjectGenerator.instance();
  protected SequenceGenerator sequenceGenerator = NullSequenceGenerator.instance();

  public EhcacheOperation<K, V> using(ObjectGenerator<K> keyGenerator, ObjectGenerator<V> valueGenerator) {
    if (this.keyGenerator instanceof NullObjectGenerator) {
      this.keyGenerator = keyGenerator;
    } else {
      throw new IllegalStateException("KeyGenerator already chosen.");
    }

    if (this.valueGenerator instanceof NullObjectGenerator) {
      this.valueGenerator = valueGenerator;
    } else {
      throw new IllegalStateException("ValueGenerator already chosen.");
    }
    return this;
  }

  public EhcacheOperation<K, V> sequentially() {
    if (this.sequenceGenerator instanceof NullSequenceGenerator) {
      this.sequenceGenerator = new IterationSequenceGenerator();
    } else {
      throw new IllegalStateException("SequenceGenerator already chosen.");
    }
    return this;
  }

  public EhcacheOperation<K, V> atRandom(Distribution distribution, long min, long max, long width) {
    if (sequenceGenerator instanceof NullSequenceGenerator) {
      this.sequenceGenerator = new RandomSequenceGenerator(distribution, min, max, width);
    } else {
      throw new IllegalStateException("SequenceGenerator already chosen");
    }
    return this;
  }

  @SuppressWarnings("unchecked")
  public EhcacheOperation<K, V> withWeight(Double weight) {
    return (EhcacheOperation<K, V>)super.withWeight(weight);
  }
}
