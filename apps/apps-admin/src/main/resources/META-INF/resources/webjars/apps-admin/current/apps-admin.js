'use strict';

define(['angular','angular-route','angular-animate','apps-admin-daemon','ui-bootstrap-tpls','ui-router-tabs','angular-ui-router'],function(angular){
    var appsAdminModule = angular.module('apps-admin',['apps-admin-daemon','ngRoute','ui.router','ui.router.tabs'
    ,'ui.bootstrap.tabs',"template/tabs/tabset.html","template/tabs/tab.html"]
    );

    appsAdminModule.config(['$stateProvider', function($stateProvider) {
                             $stateProvider
                             .state('admin', {
                                url:         '',
                                controller:  'apps-admin-ctrl',
                                templateUrl: requirejs.toUrl('apps-admin.html')
                             })
                             .state('admin.daemons', {
                                url:         '/daemons',
                                //controller:  'apps-admin-daemon-ctrl',
                                templateUrl: requirejs.toUrl('apps-admin-daemon.html')
                             })
                 }]);

    appsAdminModule.controller('apps-admin-ctrl',['$scope','$state','$stateParams',
            function($scope,$state,$stateParams){
                $scope.initialize = function(){
                    $scope.go = function(state) {
                          $state.go(state);
                        };

                    $scope.adminTabs   = [
                      {
                        heading: 'Daemons',
                        route:   'admin.daemons'
                      }/*,
                      {
                        heading: 'Accounts',
                        route:   'user.accounts'
                      }*/
                    ];
                }
                $scope.initialize();
            }]
        )
        /*.config(['$routeProvider',function($routeProvider) {
          $routeProvider
           .when('/', {
            templateUrl: requirejs.toUrl('apps-admin.html'),
            controller: 'apps-admin-ctrl'
          })}])*/;
});