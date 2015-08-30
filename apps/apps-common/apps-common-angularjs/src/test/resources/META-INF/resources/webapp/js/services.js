"use strict";

angular.module('ServiceModule', ['ngResource'])
    /**
     * service to retrieve versions of icons
     */
        .factory('TestService', ['$resource', function ($resource) {
            return $resource("/apis/tests/:name", {name:"@name"}, {});
        }]);