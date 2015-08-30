"use strict";

var testApp=angular.module('testApp',['ServiceModule','ngResource']);

testApp.controller('testCtrl',['$scope','TestService', function($scope,TestService){
    $scope.changeName=function(){
        TestService.get({name:$scope.yourName},function(data){
            $scope.toto = data.message;
        });
    }

}]);