Rainfall
========

Rainfall is an extensible java framework to implement custom DSL based stress and performance tests in your application.

It has a customisable fluent interface that lets you implement your own DSL when writing tests scenarios, and define your own tests actions and metrics.
Rainfall is open to extensions, three of which are currently in progress,
- Rainfall web is a Yet Another Web Application performance testing library
- Rainfall JCache is a library to test the performance of JSR107 caches solutions
- Rainfall Ehcache is a library to test the performance of Ehcache 2 and 3

![Built on DEV@cloud](https://www.cloudbees.com/sites/default/files/styles/large/public/Button-Built-on-CB-1.png?itok=3Tnkun-C)

[![Build Status](https://rainfall.ci.cloudbees.com/buildStatus/icon?job=Rainfall ehcache)](https://rainfall.ci.cloudbees.com/job/Rainfall%20ehcache/)

Components
----------
[Rainfall-core](https://github.com/aurbroszniowski/Rainfall-core) is the core library containing the key elements of the framework.
 When writing your framework implementation, you must include this library as a dependency.

[Rainfall-web](https://github.com/aurbroszniowski/Rainfall-web) is the Web Application performance testing implementation.

[Rainfall-jcache](https://github.com/aurbroszniowski/Rainfall-jcache) is the JSR107 caches performance testing implementation.

[Rainfall-ehcache](https://github.com/aurbroszniowski/Rainfall-ehcache) is the Ehcache 2.x/3.x performance testing implementation.


Quick start
-----------

Performance tests are written in java

```java
ObjectGenerator<Long> keyGenerator = new LongGenerator();   // (1)  
ObjectGenerator<byte[]> valueGenerator = ByteArrayGenerator.fixedLength(1000); // (2)

StatisticsPeekHolder finalStats = Runner.setUp( // (3) 
    Scenario.scenario("Test phase").exec(   // (4)
        put(Long.class, byte[].class)     // (5)   
           .using(keyGenerator, valueGenerator) // (6)
           .atRandom(GAUSSIAN, 0, 100000, 5000), // (7)
        get(Long.class, byte[].class).withWeight(0.80) //(8)
            .using(keyGenerator, valueGenerator)
            .atRandom(GAUSSIAN, 0, 100000, 5000)
    ))
    .executed(during(1, minutes))
    .config(concurrency, 
          ReportingConfig.report(EhcacheResult.class).log(text(), html()).summary(text()))
    .config(cacheConfig(Long.class, byte[].class)
        .caches(one, two, three, four).bulkBatchSize(10))
    .start();
        
  (1) A class to generate Long values
  (2) A class to generate byte arrays of 1000 bytes
  (3) Test runner, returns a StatisticsPeekHolder that holds the final stats summary
  (4) a Scenario is a list of operations that will be executed
  (5) an operation (put) that will put Long objects as keys and byte[] objects as values.
  (6) use a Long generator as a key generator and a byte array generator for values
  (7) The distribution of keys will be gaussian, to simulate a real-life scenario where 
          only a limited set of keys are more often accessed
  (8) Another operation : get()        
```

See the [Wiki](https://github.com/aurbroszniowski/Rainfall-ehcache/wiki) to list all operations and parameters.


Build the project
-----------------
```maven
  mvn clean install
```

Use it in your project
----------------------
```maven
  <dependencies>
    <dependency>
      <groupId>io.rainfall</groupId>
      <artifactId>rainfall-ehcache</artifactId>
      <version>1.0.3-SNAPSHOT</version>
    </dependency>

    <dependency>
      <groupId>io.rainfall</groupId>
      <artifactId>rainfall-core</artifactId>
      <version>1.0.3-SNAPSHOT</version>
    </dependency>
  </dependencies>
```


Thanks to the following companies for their support to FOSS:
------------------------------------------------------------

[ej-technologies for JProfiler](http://www.ej-technologies.com/products/jprofiler/overview.html)

[Sonatype for Nexus](http://www.sonatype.org/)

[Cloudbees for cloud-based continuous delivery](https://www.cloudbees.com/)

and of course [Github](https://github.com/) for hosting this project.


