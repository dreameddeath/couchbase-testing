core-service
=================

This module contains all usefull classes to expose services in a rest and distributed manner.

It uses :
- curator-x-discovery as centralized service exposition registering
- swagger for api introspection/self documentation (with a customized datamodel introspector)
- jax-rs (2.0)

It provides :
- helper classes for managing service registration (AbstractExposableService class/ServiceRegistrar)
- helper classes for managing client calls with self autodiscovery manner : ServiceClientFactory
- Rest Service to document all registered apis (and instances) : RestServiceDiscovery/ServiceDiscoverer
- annotation processor to ease the generation of client/server REST implementation for almost any Service

TODO
--------------
###features
- Reflexion around href links for services (HATEOS)
- ~~Management of stats (metrics, ...) around the registered services~~
- ~~helpers around spring service scans~~
- ~~self discovery of services should be based on path cache~~
- helpers on management of async service modes
- normalisation of error management

###testing
- ~~test the swagger api exposure~~