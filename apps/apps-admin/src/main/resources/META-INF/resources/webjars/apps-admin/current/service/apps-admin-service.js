'use strict';

define(['angular','angular-route','angular-animate','apps-admin-service-resource','ui-bootstrap-tpls'],function(angular){
    var appsServiceModule = angular.module('apps-admin-service',['apps-admin-service-resource','ngResource','ngRoute','ui.bootstrap.modal',
        'template/modal/backdrop.html',
        'template/modal/window.html'
        ]
    );
    appsServiceModule.config(['$stateProvider', function($stateProvider) {
                $stateProvider
                     .state('admin.service', {
                          url:         '/service',
                          templateUrl: requirejs.toUrl('apps-admin-service.html')
                       })
                     .state('admin.service.domain', {
                        url:         '/:domain',
                        templateUrl: requirejs.toUrl('apps-admin-service-domain.html'),
                        controller:"apps-admin-service-domain-ctrl"
                     })
                      .state('admin.service.domain.version', {
                         url:         '/:fullName',
                         templateUrl: requirejs.toUrl('apps-admin-service-version.html'),
                         controller:"apps-admin-service-version-ctrl"
                      });
         }]);


    appsServiceModule.controller('apps-admin-service-ctrl',['$scope','$state',
            'ServicesDomains',
            function($scope,$state,ServicesDomains){
                $scope.go=function(domainName){
                    $state.go("admin.service.domain",{domain:domainName});
                }
                $scope.refresh=function(){
                    ServicesDomains.get(function(data){
                        $scope.serviceDomains = [];
                        var domainPos=0;
                        for(var domainPos=0;domainPos<data.length;++domainPos){
                            $scope.serviceDomains.push({
                                name:data[domainPos],
                                isActive:function(){
                                    return $state.includes('admin.service.domain',{domain:this.name});
                                }
                            });
                        }
                    });
                }
                $scope.refresh();
            }]
        );

    appsServiceModule.controller('apps-admin-service-domain-ctrl',['$scope','$state','$stateParams'
                ,'ServicesDomainServiceInfo','ServicesDomainClientInstances',
                function($scope,$state,$stateParams,ServicesDomainServiceInfo,ServicesDomainClientInstances){
                    $scope.close=function(){
                            $state.go("^");
                       };
                    $scope.showDetails=function(fullName){
                        $state.go("admin.service.domain.version",{"fullName":fullName});
                    }

                    $scope.currServiceVersionInfo={};
                    $scope.setCurrVersion=function(fullName){
                        $scope.currVersionFullName=fullName;
                        $scope.updateCurrServiceVersion();
                    }
                    $scope.updateCurrServiceVersion=function(){
                        $scope.currServiceVersionInfo={};
                        for(var pos=0;pos<$scope.services.length;++pos){
                            var currService = $scope.services[pos];
                            for(var version in currService.versions){
                                var currVersion = currService.versions[version];
                                if(currVersion.fullName==$scope.currVersionFullName){
                                    $scope.currServiceVersionInfo={
                                            serviceName:currService.name,
                                            "version":version,
                                            swaggerUrl:encodeURIComponent("/apis/apps-admin/domains/"+encodeURIComponent($stateParams.domain)+"/swagger/"+encodeURIComponent($scope.currVersionFullName))
                                    };
                                    for(var key in currVersion){
                                        $scope.currServiceVersionInfo[key]=currVersion[key];
                                    }
                                }
                            }
                        }
                    }

                    $scope.refresh=function(){
                        $scope.services=[];
                        $scope.name=$stateParams.domain;
                        ServicesDomainServiceInfo.list({domain:$stateParams.domain},function(data){
                            for(var pos=0;pos<data.length;++pos){
                                var currService = data[pos];
                                for(var version in currService.versions){
                                    var currServiceVersion = currService.versions[version];
                                    currServiceVersion.clients=[];
                                    currServiceVersion.refreshClients=function(){
                                        //currServiceVersion.clients=[];
                                        ServicesDomainClientInstances.list({domain:$stateParams.domain,fullname:currServiceVersion.fullName},function(data){
                                            currServiceVersion.clients=data;
                                        })
                                    };
                                    currServiceVersion.refreshClients();
                                }
                                $scope.services.push({
                                    name:currService.name,
                                    versions:currService.versions,
                                    nbVersions:function(){
                                        return Object.keys(this.versions).length;
                                    }
                                });
                            }
                            $scope.updateCurrServiceVersion();
                        });
                    };
                    $scope.refresh();
                }]
            );

    appsServiceModule.controller('apps-admin-service-version-ctrl',['$scope','$state','$stateParams',
                    function($scope,$state,$stateParams){
                        $scope.$parent.setCurrVersion($stateParams.fullName);
                        $scope.showDaemon = function(daemonUid){
                            $state.go("admin.daemon.detail",{uid:daemonUid});
                        }
                        //$scope.serviceInfo = $scope.$parent.getServiceVersionInfo($scope.fullName);
                    }]
                );

}
);