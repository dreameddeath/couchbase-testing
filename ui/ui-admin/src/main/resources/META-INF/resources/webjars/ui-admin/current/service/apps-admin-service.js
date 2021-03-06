'use strict';

define(['angular','angular-route','angular-animate','apps-admin-service-resource','ui-bootstrap-tpls'],function(angular){
    var appsServiceModule = angular.module('apps-admin-service',['apps-admin-service-resource','ngResource','ngRoute','ui.bootstrap.modal',
        //'uib/template/modal/backdrop.html',
        'uib/template/modal/window.html'
        ]
    );
    appsServiceModule.config(['$stateProvider', function($stateProvider) {
                $stateProvider
                     .state('admin.service', {
                          url:         '/services',
                          templateUrl: requirejs.toUrl('apps-admin-service.html')
                       })
                     .state('admin.service.domain', {
                        url:         '/:domain',
                        templateUrl: requirejs.toUrl('apps-admin-service-domain.html'),
                        controller:"apps-admin-service-domain-ctrl"
                     })
                      .state('admin.service.domain.version', {
                         url:         '/:type/:fullName',
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

    appsServiceModule.controller('apps-admin-service-domain-ctrl',['$scope','$state','$stateParams',
                'ServicesDomainTypes','ServicesDomainServiceInfo','ServicesDomainClientInstances','ServicesDomainProxyInstances',
                function($scope,$state,$stateParams,ServicesDomainTypes,ServicesDomainServiceInfo,
                        ServicesDomainClientInstances,ServicesDomainProxyInstances)
                {
                    var ServiceVersion =function(domain,service,version,versionInfo){
                        var self=this;
                        for(var attr in versionInfo){
                            this[attr] = versionInfo[attr];
                        }
                        self.clients=[];
                        self.proxies=[];
                        self.refreshClients=function(){
                            ServicesDomainClientInstances.list({"domain":domain,type:service.techType,fullname:self.fullName},function(dataClients){
                                self.clients.length=0;
                                self.clients.push.apply(self.clients,dataClients);
                            })
                            ServicesDomainProxyInstances.list({"domain":domain,type:service.techType,fullname:self.fullName},function(dataProxies){
                                self.proxies.length=0;
                                self.proxies.push.apply(self.proxies,dataProxies);
                                //self.proxies=dataProxies;
                            });
                        };
                        self.refreshClients();
                    }


                    var Service =function(domain,serviceInfo){
                        var self=this;
                        for(var attr in serviceInfo){
                            if(attr=='versions') continue;
                            this[attr] = serviceInfo[attr];
                        }
                        this.versions={};
                        for(var version in serviceInfo.versions){
                            var currVersion = serviceInfo.versions[version];
                            this.versions[version]=new ServiceVersion(domain,self,version,currVersion);
                        }

                        this.nbVersions=function(){
                            return Object.keys(this.versions).length;
                        }
                    }

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
                                    var testToolName="unknown";
                                    var testToolLink="#";
                                    if(currService.techType=='REST'){
                                        var swaggerSpecUrl = encodeURIComponent("/apis/apps-admin/domains/"+encodeURIComponent($stateParams.domain)+"/"+encodeURIComponent(currService.techType)+"/specifications/"+encodeURIComponent($scope.currVersionFullName));
                                        var instancesGetUrl = encodeURIComponent("/apis/apps-admin/domains/"+encodeURIComponent($stateParams.domain)+"/"+encodeURIComponent(currService.techType)+"/instances/"+encodeURIComponent($scope.currVersionFullName));
                                        var proxiesGetUrl = encodeURIComponent("/apis/apps-admin/domains/"+encodeURIComponent($stateParams.domain)+"/"+encodeURIComponent(currService.techType)+"/proxies/"+encodeURIComponent($scope.currVersionFullName));
                                        testToolName='Swagger';
                                        testToolLink = requirejs.toUrl('apps-admin-service-swagger.html')+
                                                    "?url="+swaggerSpecUrl
                                                    +"&instances="+instancesGetUrl
                                                    +"&proxies="+proxiesGetUrl
                                                    ;
                                    }
                                    $scope.currServiceVersionInfo={
                                            serviceName:currService.name,
                                            serviceType:currService.techType,
                                            "version":version,
                                            "testToolName":testToolName,
                                            "testToolLink":testToolLink
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
                        ServicesDomainTypes.get({domain:$stateParams.domain},function(typeListData){
                            for(var posTypeList=0;posTypeList<typeListData.length;++posTypeList){
                                ServicesDomainServiceInfo.list({domain:$stateParams.domain,type:typeListData[posTypeList]},function(data){
                                    for(var pos=0;pos<data.length;++pos){
                                        $scope.services.push( new Service($stateParams.domain,data[pos]));
                                    }
                                    $scope.updateCurrServiceVersion();
                                })
                            }
                        })
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
                    }]
                );

}
);