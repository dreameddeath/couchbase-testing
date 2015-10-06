"use strict";

define(['angular','angular-resource'],function(angular){
    var appsAdminResourceModule = angular.module('apps-admin-resource', ['ngResource']);
    appsAdminResourceModule.factory('DaemonsListService', ['$resource', function ($resource) {
        return $resource("/apis/apps-admin/daemons", null, { 'get':  {method:'GET',isArray:true}});
    }]);
    appsAdminResourceModule.factory('DaemonInfoService', ['$resource', function ($resource) {
        return $resource("/apis/apps-admin/daemons/:uid", {uid:"@uid"}, { 'get':  {method:'GET'}});
    }]);
    appsAdminResourceModule.factory('DaemonStatusService', ['$resource', function ($resource) {
        return $resource("/apis/apps-admin/daemons/:uid/status", {uid:"@uid"}, { 'get':  {method:'GET'},'put':{method:'PUT'}});
    }]);
    appsAdminResourceModule.factory('DaemonWebServersListService', ['$resource', function ($resource) {
        return $resource("/apis/apps-admin/daemons/:uid/webservers", {uid:"@uid"}, { 'get':  {method:'GET',isArray:true}});
    }]);
    appsAdminResourceModule.factory('DaemonWebServerInfoService', ['$resource', function ($resource) {
        return $resource("/apis/apps-admin/daemons/:uid/webservers/:wid", {uid:"@uid",wid:"@wid"},{ 'get':  {method:'GET'}});
    }]);
    appsAdminResourceModule.factory('DaemonWebServerStatusService', ['$resource', function ($resource) {
        return $resource("/apis/apps-admin/daemons/:uid/webservers/:wid/status", {uid:"@uid",wid:"@wid"}, { 'get':  {method:'GET'},'put':{method:'PUT'}});
    }]);
});