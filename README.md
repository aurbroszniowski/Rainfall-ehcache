Rainfall-ehcache
================

Rainfall is an extensible java framework to implement custom DSL based stress and performance tests in your application.

It has a customisable fluent interface that lets you implement your own DSL when writing tests scenarios, and define your own tests actions and metrics.
Rainfall is open to extensions, three of which are currently in progress,
- Rainfall web is a Yet Another Web Application performance testing library
- Rainfall JCache is a library to test the performance of JSR107 caches solutions
- Rainfall Ehcache is a library to test the performance of Ehcache 2 and 3

Quick start
-----------

Performance tests are written in java

```java
ObjectGenerator<Long> keyGenerator = new LongGenerator();   // (1)  
ObjectGenerator<byte[]> valueGenerator = ByteArrayGenerator.fixedLength(1000); // (2)

StatisticsPeekHolder finalStats = Runner.setUp( // (3) 
    Scenario.scenario("Test phase").exec(   // (4)
        weighted(0.10, put(keyGenerator, valueGenerator, // (5)
                atRandom(GAUSSIAN, 0, 100000, 5000), // (6)
                Coolections.singletonList(cache("cacheOne", cacheOne)))), // (7)
        weighted(0.90, get(keyGenerator, valueGenerator, // (8)
                atRandom(GAUSSIAN, 0, 100000, 5000),
                Arrays.asList(cache("cacheOne", cacheOne), cache("cacheTwo", cacheTwo)))) // (9)
    ))
    .warmup(during(30, seconds)) // (10)
    .executed(during(1, minutes))
    .config(concurrency, 
          ReportingConfig.report(EhcacheResult.class).log(text(), ReportingConfig.hlog()).summary(text())) //(11)
    .start();
```
 
```text
  (1) A class to generate Long values
  (2) A class to generate byte arrays of 1000 bytes
  (3) Test runner, returns a StatisticsPeekHolder that holds the final stats summary
  (4) A Scenario is a list of operations that will be executed
  (5) A weighted operation (put), which occurs 10% of the times, using a Long generator as a key generator and a byte array generator for values
  (6) The distribution of keys will be gaussian, to simulate a real-life scenario where 
          only a limited set of keys are more often accessed
  (7) The operation will be executed on the single cache given as parameter 
  (8) Another operation : get(), executed 90% of the times        
  (9) Here, a list of two caches is used
  (10) The Scenario will be have a warmup time of 30 seconds and the be executed during 1 minute
  (11) The report will be both text and HdrHistogram-based
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
      <version>LATEST</version>
    </dependency>
  </dependencies>
```


Thanks to the following companies for their support to FOSS:
------------------------------------------------------------

[Sonatype for Nexus](http://www.sonatype.org/)

and of course [Github](https://github.com/) for hosting this project.


