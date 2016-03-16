"use strict";

define(['angular','angular-resource'],function(angular){
    var resourceModule = angular.module('apps-admin-process-resource', ['ngResource']);
    resourceModule.factory('TasksListService', ['$resource', function ($resource) {
        return $resource("/apis/apps-admin/processors/tasks", null, { 'get':  {method:'GET',isArray:true}});
    }]);
    resourceModule.factory('TasksInfoService', ['$resource', function ($resource) {
        return $resource("/apis/apps-admin/processors/tasks/:uid", {uid:"@uid"}, { 'get':  {method:'GET'}});
    }]);

    resourceModule.factory('JobsListService', ['$resource', function ($resource) {
        return $resource("/apis/apps-admin/processors/jobs", null, { 'get':  {method:'GET',isArray:true}});
    }]);
    resourceModule.factory('JobsInfoService', ['$resource', function ($resource) {
        return $resource("/apis/apps-admin/processors/jobs/:uid", {uid:"@uid"}, { 'get':  {method:'GET'}});
    }]);
});