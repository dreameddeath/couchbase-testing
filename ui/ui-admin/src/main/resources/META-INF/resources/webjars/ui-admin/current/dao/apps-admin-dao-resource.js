"use strict";

define(['angular','angular-resource'],function(angular){
    var resourceModule = angular.module('apps-admin-dao-resource', ['ngResource']);

    resourceModule.factory('DaoDomainsService', ['$resource', function ($resource) {
                return $resource("/apis/apps-admin/daos/domains", null, {'get':  {method:'GET',isArray:true}});
    }]);

    resourceModule.factory('DaoListService', ['$resource', function ($resource) {
        return $resource("/apis/apps-admin/daos/list", null, { 'get':  {method:'GET',isArray:true}});
    }]);


    resourceModule.factory('DaoInfoService', ['$resource', function ($resource) {
        return $resource("/apis/apps-admin/daos/instance/:uid", {uid:"@uid"}, { 'get':  {method:'GET'}});
    }]);
});