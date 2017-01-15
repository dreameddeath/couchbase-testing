'use strict';

define(['angular','angular-route','angular-animate','apps-admin-config-list','ui-bootstrap-tpls','ui-router-tabs','angular-ui-router'],function(angular){
    var appsConfigModule = angular.module('apps-admin-config-shared',
        ['apps-admin-config-list','ngResource','ngRoute','ui.router','ui.router.tabs'
        ,'ui.bootstrap.tabs',"uib/template/tabs/tabset.html","uib/template/tabs/tab.html"
    ]);

    appsConfigModule.config(['$stateProvider', function($stateProvider) {
                 $stateProvider
                  .state('admin.config', {
                   url:         '/config',
                   templateUrl: requirejs.toUrl('apps-admin-config-shared.html')
                })
                 .state('admin.config.domain', {
                    url:         '/:domain',
                    templateUrl: requirejs.toUrl('apps-admin-config-list.html'),
                    resolve :{
                        configDomainDef:function($stateParams){
                            return {
                                'type':'shared',
                                'domain':$stateParams.domain
                            };
                        }
                    } ,
                    controller:"apps-admin-config-list-ctrl"
                 })
                 .state('admin.config.domain.add', {
                     url:        '/add',
                     onEnter: ['AppsAdminConfigListAddModal','$state','$stateParams',
                     function(AppsAdminConfigListAddModal,$state,$stateParams){
                             AppsAdminConfigListAddModal.open($state,{
                                     'type':'shared',
                                     'domain':$stateParams.domain
                             });
                         }
                     ]
                 })
     }]);

    appsConfigModule.controller('apps-admin-config-shared-ctrl',['$scope','$state','$stateParams','SharedConfigs',
                function($scope,$state,$stateParams,SharedConfigs){
                    $scope.initialize = function(){
                        $scope.go = function(domain) {
                              $state.go('admin.config.domain',{"domain":domain});
                            };

                        $scope.adminSharedConfig   = [];
                        SharedConfigs.get(function(data){
                            for(var pos=0;pos<data.length;++pos){
                                $scope.adminSharedConfig.push({
                                        domain:data[pos],
                                        isActive:function(){
                                            return $state.includes('admin.config.domain',{domain:this.domain});
                                        }
                                 });
                             }
                        });

                    }
                    $scope.initialize();
                }]
            );




});