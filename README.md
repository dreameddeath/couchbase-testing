couchbase-testing
=================

[![Build Status](https://travis-ci.org/dreameddeath/couchbase-testing.png?branch=master)](https://travis-ci.org/dreameddeath/couchbase-testing)
[![Coverage Status](https://coveralls.io/repos/dreameddeath/couchbase-testing/badge.svg?branch=master&service=github)](https://coveralls.io/github/dreameddeath/couchbase-testing?branch=master)

TODOs
-----

### Couchbase Layer
- ~~Metric Gathering~~
- ~~Views/Queries~~
- ~~Getting from Replicate Management (load-balancing)~~
- ~~Simulation (in-memory mode) for all verbs (get, update, append,prepend, ...) for views also and cas~~
- Simulation of expiration
- management of time to simulate data expiration
- ~~Replicate/Timeout/Persistance Management (ReplicateTo/PersistTo)~~
- ~~Unique Index~~
- ~~Datamodel upgrade management~~
- History :
    - Keep previous revision at document level
    - Audited Fields
    - Per session, Per Update history
    - External Bucket History
    - History Life cycle (cf life cycle of docs)
    - History can be gzipped or not
    - Management of DataModel Updates
- Life Cycle :
    * Short Term : Pure TTL with per access extension (touch, ...)
    * Long Term :
        - With direct Delete
        - With Archive + Delete
        - With Archive + Delete after TTL
        - Archive in Zipped format or not
        - Management of DataModel Updates for archives (or use pure anonymous archive? generic json for instance ?)

### Model layer
- Be able to generate an model introspector (to extract XSD or Json-schema) - better at compile time
- Generate classes base on XML Schema or Json similar (include datamodel upgrade definition)
- Visitor Tool
- Validator rewrite


### Framework Layer
- ~~event based notification~~
- ~~MDC integration~~
- ~~Unified SOAP & REST API (Use of jibx mappings - or use jackson xml)~~
- Configuration (properties + db config + overriding management) + AutoReload - timebase

### Infrastructure Layer
- Logging management (local + distributed & correlation) - LogStash
- Security Layer (and Auditing)
- ~~Configuration (and configuration override/deploy) - have look at Netflix's archaius~~
- ~~Perfs/Stats Monitoring (using - dropwizard metrics)~~
- Version Upgrades
- Integration with Storm - DRPC, standard - , kafka (& ElasticSearch, Esper?)
- ~~Integration with Apache Curator (zookeeper, service dispatch)~~

### Testing
- Job Unit Tests (~ integration tests) with or without calc only, db simulator
- Job Unit Tests with failure automation
- Job Unit Tests with race condition
- Unit Tests/Integration Test/Non regression Dataset Management tools :
     * Json/XML based patch / checks definition
     * Database loading with in memory (calc only) or real database insert
     * tools for easy BDD management (ex using Cucumber-JVM for instance)
- Overriding of logs levels for tests/production purposes

### Business Layer
- Catalog Cache and version deployment (reuse of config checks) using change-sets as a base
- Rating with 3 modes (rating engine itself doesn't matter) :
    * Batch (incremental, full context rerate,...)
    * Near-Realtime
    * RealTime
    * use of logs for analysis (+elasticsearch)
- Billing (phases of billing - change of cycle, ...).
    * Here also parts of engine (discount, fees billing, ledger,...)
    * Should be replaceable
- Installed Base updates management

###Admin
- IHM (AngularJS)
