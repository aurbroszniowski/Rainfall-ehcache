package io.rainfall.ehcache2.operation;

import io.rainfall.ObjectGenerator;
import io.rainfall.Operation;
import io.rainfall.SequenceGenerator;
import io.rainfall.generator.IterationSequenceGenerator;
import io.rainfall.generator.RandomSequenceGenerator;
import io.rainfall.generator.sequence.Distribution;
import io.rainfall.utils.NullObjectGenerator;
import io.rainfall.utils.NullSequenceGenerator;

/**
 * @author Aurelien Broszniowski
 */

public abstract class EhcacheOperation<K, V> extends Operation {

  ObjectGenerator<K> keyGenerator = NullObjectGenerator.instance();
  ObjectGenerator<V> valueGenerator = NullObjectGenerator.instance();
  SequenceGenerator sequenceGenerator = NullSequenceGenerator.instance();

  @SuppressWarnings("unchecked")
  public EhcacheOperation<K, V> using(ObjectGenerator keyGenerator, ObjectGenerator valueGenerator) {
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
