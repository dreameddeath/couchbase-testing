'use strict';

define(['angular','ui-router-lazy-load','angular-js-css-file',
        'ui-bootstrap-tpls','ui-router-tabs','angular-ui-router'],function(angular){

    var appsAdminModule = angular.module('apps-admin',[
        'ui-router-lazy-load','angular-js-css-file',
      'ui.router','ui.router.tabs'
    ,'ui.bootstrap.tabs',"template/tabs/tabset.html","template/tabs/tab.html"]
    );



    appsAdminModule.config(['$stateProvider','$urlRouterProvider', function($stateProvider,$urlRouterProvider) {
                             //$urlRouterProvider.otherwise("/");
                             $stateProvider
                             .state('admin', {
                                url:         '',
                                controller:  'apps-admin-ctrl',
                                templateUrl: requirejs.toUrl('apps-admin.html')
                             });
                 }]);

    appsAdminModule.config(['$futureStateProvider', function($futureStateProvider) {
        $futureStateProvider.futureState({
            'stateName': 'admin.service',
            'urlPrefix': '/services',
            'type': 'ocLazyLoad',
            'moduleDef':{
                name: 'apps-admin-service',
                files: ['apps-admin-service']
            }
        });
        $futureStateProvider.futureState({
            'stateName': 'admin.daemon',
            'urlPrefix': '/daemons',
            'type': 'ocLazyLoad',
            'moduleDef':{
                name: 'apps-admin-daemon',
                files: ['apps-admin-daemon']
            }
        });
        $futureStateProvider.futureState({
            'stateName': 'admin.dao',
            'urlPrefix': '/daos',
            'type': 'ocLazyLoad',
            'moduleDef':{
                name: 'apps-admin-dao',
                files: ['apps-admin-dao']
            }
        });
        $futureStateProvider.futureState({
            'stateName': 'admin.config',
            'urlPrefix': '/configs',
            'type': 'ocLazyLoad',
            'moduleDef':{
                name: 'apps-admin-config-shared',
                files: ['apps-admin-config-shared']
            }
        });
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
        );
});