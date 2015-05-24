infrastructure-kafka module
================================

This module is used to provide helper classes to ease and standardize the integration with kafka queuing system

todo
-------------------
- build a standardized abstract message (maybe move it in the core layer)
- Build a standardized serializer/deserializer
- build a mecanism to automatise topic creations (if possible)
- build a configurable dispatcher among queues (topic/partition) and standardized consumer daemons for non storm consumers