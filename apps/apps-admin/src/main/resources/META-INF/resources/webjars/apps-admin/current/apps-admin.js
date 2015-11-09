'use strict';

define(['angular','angular-route','angular-animate',
        'apps-admin-daemon','apps-admin-dao',
        'ui-bootstrap-tpls','ui-router-tabs','angular-ui-router'],function(angular){
    var appsAdminModule = angular.module('apps-admin',['apps-admin-daemon','apps-admin-dao','ngRoute','ui.router','ui.router.tabs'
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
                                templateUrl: requirejs.toUrl('apps-admin-daemon.html')
                             })
                             .state('admin.daos', {
                                 url:         '/daos',
                                 templateUrl: requirejs.toUrl('apps-admin-dao.html')
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
                      },
                      {
                        heading: 'Daos',
                        route:   'admin.daos'
                      }
                    ];
                }
                $scope.initialize();
            }]
        )
});