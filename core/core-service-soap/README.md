core-service-soap
======================

The purpose of this module is to provide tools to manage soap web service like rest one :
* Server level :
     * Register soap instance per version (and wsdl)
     * API to list exposed local instances
     * prepare for proxy for load balancing
* Client level :
     * Provide a wrapper to automate service call with load balancing (should be at endpoint level)
     * Register soap Clients (like rest one) to ease dependency follow-up

TODO
--------------------------
Everything
