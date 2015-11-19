"use strict";

define(['angular','angular-resource'],function(angular){
    var appsConfigResourceModule = angular.module('apps-admin-config-resource', ['ngResource']);
    appsConfigResourceModule.factory('DaemonConfigList', ['$resource', function ($resource) {
        return $resource("/apis/apps-admin/daemons/:uid/config/:domain",  {uid:"@uid",domain:"@domain"},
        { 'get':  {method:'GET'},
          'update':  {method:'PUT'},
          'delete':  {method:'DELETE'}
        });
    }]);
    appsConfigResourceModule.factory('DaemonConfigItem', ['$resource', function ($resource) {
            return $resource("/apis/apps-admin/daemons/:uid/config/:domain/:key", {uid:"@uid",domain:"@domain",key:"@key"},
            { 'get':  {method:'GET'},
              'update':  {method:'PUT'},
              'delete':  {method:'DELETE'}
            });
        }]);

    appsConfigResourceModule.factory('SharedConfigList', ['$resource', function ($resource) {
            return $resource("/apis/apps-admin/config/:domain",  {domain:"@domain"},
            { 'get':  {method:'GET'},
              'update':  {method:'PUT'},
              'delete':  {method:'DELETE'}
            });
        }]);
    appsConfigResourceModule.factory('SharedConfigItem', ['$resource', function ($resource) {
            return $resource("/apis/apps-admin/config/:domain/:key", {domain:"@domain",key:"@key"},
            { 'get':  {method:'GET'},
              'update':  {method:'PUT'},
              'delete':  {method:'DELETE'}
            });
        }]);

});