Deployement Sub domain
=================

Logic
-----
- A repository of MicroApps (apps name, version, checksum)
- A repository of shared resources (NFS directories, ...)
- A µApp must provide a "Main Function" for Deployment
- This Main is in charge of executing steps regarding the current step "attained"
- Each steps will have :
     - Deploy :
        - Pre-requisites Checks
        - Actions
        - Post Requisites Checks
     - Rollback 
         - Deploy Steps it applies to

Type of Actions
----------
- Upload of MicroApp toward agents
- Deploy directories withing Agents
- Activate a Version MicroApps version on some Agent
- Desactivate a Version MicroApps on some Agent
- Change Centralized config
- Couchbase Specific :
    * Create View
    * Remove View
    * Massive Upgrade of datamodel
- ElasticSearch Specific :
    * Create Index
    * Remove Index
- Change local persistent conf

Type of Pre-requisites
----------
- µApps :
    * Version Deployment
    * Deployment state
- Services :
    * Nb active services
- Couchbase :
    * Cluster Up and Running
    * Bucket Existing
    * View Existing
- Elastic-search :
    * Cluster Up and Running
    * Index existing
    * Index not existing
- Kafka :
    * Topic Existing
    * Partitions
- Config :
    * Config Value check...
- Zookeeper :
    * Node Existing