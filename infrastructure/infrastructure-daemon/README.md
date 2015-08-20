infrastructure-daemon
================================

Set of wrapper classes to ease the creation of normalized supervised daemons with
- command line parser
- standardize configuration init (zookeeper, ...)
- spring based pre-integration
- expose standardized REST api for start/stop/restart/reload/status/...
- expose standardized api for supervision ( introspections, metrics, ...)
- expose classes for various daemon utils (web server, ...)
- self registering utilities (instance, ...)


TODO
-------------------
Zookeeper daemon registering :
- register a given daemon as itself somewhere in zookeeper
- provide services to navigate within those information 

Architecture :
- separate the admin part and the business part