"use strict";

var testApp=angular.module('testApp',[/*'ServiceModule',*/'ngResource']);

testApp.controller('testCtrl',['$scope'/*,'TestService'*/, function($scope,TestService){
    $scope.toto="default value";
    $scope.changeName=function(){
        //console.log("Changing name to "+$scope.yourName);
        $scope.toto="default value:"+$scope.yourName;
        /*TestService.get({name:$scope.yourName},function(data){
            $scope.toto = data.message;
        });*/
    }

}]);