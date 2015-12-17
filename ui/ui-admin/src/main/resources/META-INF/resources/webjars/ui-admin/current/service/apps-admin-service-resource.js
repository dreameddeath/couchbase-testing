"use strict";

define(['angular','angular-resource'],function(angular){
    var resourceModule = angular.module('apps-admin-service-resource', ['ngResource']);
    resourceModule.factory('ServicesDomains', ['$resource', function ($resource) {
        return $resource("/apis/apps-admin/domains", null, { 'get':  {method:'GET',isArray:true}});
    }]);
    resourceModule.factory('ServicesDomainServiceInfo', ['$resource', function ($resource) {
        return $resource("/apis/apps-admin/domains/:domain/infos/:name", {domain:"@domain",name:"@name"},
            { 'list':  {method:'GET',isArray:true},
              'get':{method:'GET'}
            });
    }]);

     resourceModule.factory('ServicesDomainServiceInstances', ['$resource', function ($resource) {
            return $resource("/apis/apps-admin/domains/:domain/instances/:fullname/:id", {domain:"@domain",fullname:"@fullname",id:"@id"},
                { 'list':  {method:'GET',isArray:true},
                  'instance':{method:'GET'}
                });
        }]);

     resourceModule.factory('ServicesDomainClientInstances', ['$resource', function ($resource) {
                 return $resource("/apis/apps-admin/domains/:domain/clients/:fullname/:id", {domain:"@domain",fullname:"@fullname",id:"@id"},
                     { 'list':  {method:'GET',isArray:true},
                       'instance':{method:'GET'}
                     });
             }]);

     resourceModule.factory('ServicesDomainProxyInstances', ['$resource', function ($resource) {
                      return $resource("/apis/apps-admin/domains/:domain/proxies/:fullname/:id", {domain:"@domain",fullname:"@fullname",id:"@id"},
                          { 'list':  {method:'GET',isArray:true},
                            'instance':{method:'GET'}
                          });
                  }]);
});