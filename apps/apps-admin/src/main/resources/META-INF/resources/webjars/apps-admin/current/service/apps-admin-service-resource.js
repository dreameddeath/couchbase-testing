"use strict";

define(['angular','angular-resource'],function(angular){
    var resourceModule = angular.module('apps-admin-service-resource', ['ngResource']);
    resourceModule.factory('ServicesDomains', ['$resource', function ($resource) {
        return $resource("/apis/apps-admin/services", null, { 'get':  {method:'GET',isArray:true}});
    }]);
    resourceModule.factory('ServicesDomains', ['$resource', function ($resource) {
        return $resource("/apis/apps-admin/services/:uid", {uid:"@uid"}, { 'get':  {method:'GET'}});
    }]);
    resourceModule.factory('DaemonStatusService', ['$resource', function ($resource) {
        return $resource("/apis/apps-admin/daemons/:uid/status", {uid:"@uid"}, { 'get':  {method:'GET'},'put':{method:'PUT'}});
    }]);
    resourceModule.factory('DaemonWebServersListService', ['$resource', function ($resource) {
        return $resource("/apis/apps-admin/daemons/:uid/webservers", {uid:"@uid"}, { 'get':  {method:'GET',isArray:true}});
    }]);
    resourceModule.factory('DaemonWebServerInfoService', ['$resource', function ($resource) {
        return $resource("/apis/apps-admin/daemons/:uid/webservers/:wid", {uid:"@uid",wid:"@wid"},{ 'get':  {method:'GET'}});
    }]);
    resourceModule.factory('DaemonWebServerStatusService', ['$resource', function ($resource) {
        return $resource("/apis/apps-admin/daemons/:uid/webservers/:wid/status", {uid:"@uid",wid:"@wid"}, { 'get':  {method:'GET'},'put':{method:'PUT'}});
    }]);
});