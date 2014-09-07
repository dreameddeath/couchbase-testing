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

Framework Layer :
- Job Unit Tests (calc only mode)
- Automated Errors Tests
- Integration with Storm
- Unified SOAP & REST API

Business Layer :
- Catalog Cache and version deployment
- Rating with 3 modes (rating engine itself doesn't matter) :
    * Batch (incremental, ...)
    * Near-Realtime
    * RealTime
- Billing (phases of billing - change of cycle, ...)
- Installed Base updates management

Admin :
- IHM (AngularJS)