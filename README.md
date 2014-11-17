couchbase-testing
=================

[![Build Status](https://travis-ci.org/dreameddeath/couchbase-testing.png?branch=master)](https://travis-ci.org/dreameddeath/couchbase-testing)

TODOs
-----

### Infrastructure Layer
- Logging management
- Security Layer (and Auditing)
- Configuration (and configuration override/deploy)
- Perfs/Stats Monitoring
- Version Upgrades

### Couchbase Layer
- Views
- ~~Unique Index (remain tests plus delete management)~~
- Datamodel upgrade management
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

### Framework Layer
- Be able to generate an model introspector (to extract XSD or Json-schema)
- Generate classes base on XML Schema or JSon similar (include datamodel upgrade definition)
- MDC integration
- Integration with Storm, kafka (& ElasticSearch, Esper?)
- Unified SOAP & REST API (Use of jibx code gen)

### Testing
- Job Unit Tests (~ integration tests) with or without calc only
- Job Unit Tests with failure automation
- Job Unit Tests with race condition
- Unit Tests/Integration Test/Non regression Dataset Management tools :
     * Json/XML based patch / checks definition
     * Database loading with in memory (calc only) or real database insert
- Overriding of logs for tests/production purposes

### Business Layer
- Catalog Cache and version deployment
- Rating with 3 modes (rating engine itself doesn't matter) :
    * Batch (incremental, full context rerate,...)
    * Near-Realtime
    * RealTime
- Billing (phases of billing - change of cycle, ...)
- Installed Base updates management

###Admin
- IHM (AngularJS)