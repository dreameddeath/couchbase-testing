"use strict";

define(['angular','angular-resource'],function(angular){
    var resourceModule = angular.module('apps-admin-dao-resource', ['ngResource']);
    resourceModule.factory('DaoListService', ['$resource', function ($resource) {
        return $resource("/apis/apps-admin/daos", null, { 'get':  {method:'GET',isArray:true}});
    }]);
    resourceModule.factory('DaoInfoService', ['$resource', function ($resource) {
        return $resource("/apis/apps-admin/daos/:uid", {uid:"@uid"}, { 'get':  {method:'GET'}});
    }]);
});