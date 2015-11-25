"use strict";

define(['angular','angular-resource'],function(angular){

    var transformResponsePlainText=function(data,headersResponse,statusResponse){
        return {content:data,headers:headersResponse,status:statusResponse};
    }
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
            { 'get':  {method:'GET',headers:{'Content-Type':'text/plain'},transformResponse:transformResponsePlainText},
              'add':  {method:'POST',headers:{'Content-Type':'text/plain'}/*,transformResponse:transformResponsePlainText*/},
              'update':  {method:'PUT',headers:{'Content-Type':'text/plain'}/*,transformResponse:transformResponsePlainText*/},
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
            { 'get':  {method:'GET',headers:{'Content-Type':'text/plain'},transformResponse:transformResponsePlainText},
              'add':  {method:'POST',headers:{'Content-Type':'text/plain'}/*transformResponse:transformResponsePlainText*/},
              'update':  {method:'PUT',headers:{'Content-Type':'text/plain'}/*transformResponse:transformResponsePlainText*/},
              'delete':  {method:'DELETE'}
            });
        }]);

});