couchbase-testing
=================

TODOs

Infrastructure Layer :
- Logging management
- Security Layer (and Auditing)
- Configuration (and configuration override/deploy)
- Perfs/Stats Monitoring
- Version Upgrades

Couchbase Layer :
- Views
- Unique Index
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
        - Management of DataModel Updates for archives

Framework Layer :
- Job Unit Tests (calc only mode)
- Automated Errors Tests
- Integration with Storm, kafka (& ElaticSearch, Esper?)
- Unified SOAP & REST API

Business Layer :
- Catalog Cache and version deployment
- Rating with 3 modes (rating engine itself doesn't matter) :
    * Batch (incremental, full context rerate,...)
    * Near-Realtime
    * RealTime
- Billing (phases of billing - change of cycle, ...)
- Installed Base updates management

Admin :
- IHM (AngularJS)