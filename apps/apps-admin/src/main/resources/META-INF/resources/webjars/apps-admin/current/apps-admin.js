'use strict';

define(['angular','angular-route','angular-animate',
        'apps-admin-daemon','apps-admin-dao','apps-admin-config-shared','apps-admin-service',
        'ui-bootstrap-tpls','ui-router-tabs','angular-ui-router'],function(angular){
    var appsAdminModule = angular.module('apps-admin',[
        'apps-admin-daemon','apps-admin-dao','apps-admin-config-shared','apps-admin-service',
        'ngRoute','ui.router','ui.router.tabs'
    ,'ui.bootstrap.tabs',"template/tabs/tabset.html","template/tabs/tab.html"]
    );

    appsAdminModule.config(['$stateProvider', function($stateProvider) {
                             $stateProvider
                             .state('admin', {
                                url:         '',
                                controller:  'apps-admin-ctrl',
                                templateUrl: requirejs.toUrl('apps-admin.html')
                             })
                             .state('admin.daemon', {
                                url:         '/daemons',
                                templateUrl: requirejs.toUrl('apps-admin-daemon.html')
                             })
                             .state('admin.dao', {
                                 url:         '/daos',
                                 templateUrl: requirejs.toUrl('apps-admin-dao.html')
                              })
                              .state('admin.config', {
                                  url:         '/config',
                                  templateUrl: requirejs.toUrl('apps-admin-config-shared.html')
                               })
                               .state('admin.service', {
                                 url:         '/service',
                                 templateUrl: requirejs.toUrl('apps-admin-service.html')
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
                        route:   'admin.daemon'
                      },
                      {
                        heading: 'Daos',
                        route:   'admin.dao'
                      },
                      {
                        heading: 'Shared Config',
                        route:   'admin.config'
                      },
                       {
                          heading: 'Services',
                          route:   'admin.service'
                        }
                    ];
                }
                $scope.initialize();
            }]
        )
});