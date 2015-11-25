'use strict';

define(['angular','angular-route','angular-animate','apps-admin-config-resource','ui-bootstrap-tpls','angular-ui-router'],function(angular){
    var appsConfigModule = angular.module('apps-admin-config-list',
        ['apps-admin-config-resource','ngResource','ngRoute','ui.router'
    ]);

    appsConfigModule.factory('AppsAdminConfigListAddModal', ['$uibModal',
        function($uibModal){
            return {
                open :function($state,configDomainDef){
                    $uibModal.open({
                            templateUrl: requirejs.toUrl('apps-admin-config-list-add.html'),
                            controller: 'apps-admin-config-list-add-ctrl',
                            resolve:{
                                "configDomainDef":configDomainDef
                            }
                        }).result.then(
                            function() {
                                $state.go('^',null,{reload:true});
                            },
                            function(){
                                $state.go('^');
                            }
                        )
                }
            }
        }
    ]);

    appsConfigModule.controller("apps-admin-config-list-ctrl",['$scope',
        'configDomainDef','$state',
        'DaemonConfigList','DaemonConfigItem','SharedConfigList','SharedConfigItem',
        function($scope,configDomainDef,$state,DaemonConfigList,DaemonConfigItem,SharedConfigList,SharedConfigItem){
            var ConfigEntry = function(key,value){
                    var self=this;
                    this.domainDef= configDomainDef;

                    this.name=key;
                    this.value=value;
                    this.newValue=null;
                    this.updateValue=function(updatedValue){
                        self.value=updatedValue;
                        self.newValue=null;
                    }
                    this.manageLocalGetResponse=function(response){
                        self.updateValue(response.content);
                    }
                    this.manageLocalUpdateResponse=function(response){
                        self.updateValue(response.newValue);
                    }
                    this.manageLocalDeleteResponse=function(response){
                        $scope.deleteKey(self.name);
                    }
                    if(self.domainDef.type=='daemon'){
                        var queryParamDef = {
                            uid:self.domainDef.uuid,
                            domain:self.domainDef.domain,
                            key:self.name
                        };
                        this.save=function(){
                            DaemonConfigItem.update(queryParamDef,self.newValue,self.manageLocalUpdateResponse);
                        };
                        this.refresh=function(){
                            DaemonConfigItem.get(queryParamDef,self.manageLocalGetResponse);
                        }
                        this.remove=function(){
                            DaemonConfigItem.delete(queryParamDef,null,self.manageLocalDeleteResponse);
                        }
                    }
                    else{
                        var queryParamDef = {
                            domain:self.domainDef.domain,
                            key:self.name
                        };
                        this.save=function(){
                            SharedConfigItem.update(queryParamDef,self.newValue,self.manageLocalUpdateResponse);
                        };
                        this.refresh=function(){
                            SharedConfigItem.get(queryParamDef,self.manageLocalGetResponse);
                        }
                        this.remove=function(){
                            SharedConfigItem.delete(queryParamDef,null,self.manageLocalDeleteResponse);
                        }
                    }
                    this.isModified=function(){
                        return self.newValue!=null && self.newValue!="" && self.value==self.newValue;
                    }
            };
            if(configDomainDef.type=='daemon'){
                //var daemonInfo = $state.data.daemonInfo;
                $scope.title="Config for daemon " +configDomainDef.uuid+ " and domain "+configDomainDef.domain;
            }
            else{
                $scope.title="Config for domain "+configDomainDef.domain;
            }
            $scope.configEntries = {};
            $scope.modifiedEntriesForUpdate=function(){
                var result=[];
                for(var key in $scope.configEntries){
                    if($scope.configEntries[key].isModified()){
                        result[key]=$scope.configEntries[key].newValue;
                    }
                }
                return result;
            }
            $scope.partialRefresh=function(newValues){
                 //var newValues = newValuesRaw.toJSON();
                 for(var key in newValues){
                    if($scope.configEntries[key] !=null){
                        $scope.configEntries[key].updateValue(newValues[key]);
                    }
                    else{
                        $scope.configEntries[key] = new ConfigEntry(key,newValues[key]);
                    }
                 }
            };

            $scope.configEntriesArray=function(){
                return Object.keys($scope.configEntries).map(function (key) {
                    return $scope.configEntries[key];
                });
            };

            $scope.updateResultRefresh=function(updateResultRaw){
                var updateResult = updateResultRaw.toJSON();
                $scope.configEntries={};
                var partialRefreshData={};
                for(var pos=0;pos<updateResult.length;++pos){
                    partialRefreshData[updateResult[pos].key]=partialRefreshData[pos].newValue;
                }
                $scope.partialRefresh(partialRefreshData);
            }

            $scope.deleteKey=function(key){
                delete $scope.configEntries[key];
            }

            $scope.fullRefresh=function(newValues){
                $scope.configEntries={};
                $scope.partialRefresh(newValues.toJSON());
            }
            if(configDomainDef.type == 'shared'){
                var queryParamDef = {
                    domain:configDomainDef.domain
                };
                $scope.refresh = function(){
                    SharedConfigList.get(queryParamDef,$scope.fullRefresh)
                };
                $scope.save=function(){
                    var updateRequest = $scope.modifiedEntriesForUpdate();
                    SharedConfigList.update(queryParamDef,updateRequest,function(){
                            this.partialRefresh(updateRequest);
                        }
                    );
                };
                $scope.add=function(){
                    $state.go(".add");
                }
            }
            else if(configDomainDef.type == 'daemon'){
                var queryParamDef = {
                    "uid":configDomainDef.uuid,
                    "domain":configDomainDef.domain
                };
                $scope.refresh = function(){
                    DaemonConfigList.get(queryParamDef,$scope.fullRefresh)
                };

                $scope.save=function(){
                    var updateRequest = $scope.modifiedEntriesForUpdate();
                        DaemonConfigList.update(queryParamDef,updateRequest,function(){
                                this.partialRefresh(updateRequest);
                            }
                    );
                };
            }
            $scope.refresh();

            $scope.closeConfig=function(){
                $state.go('^');
            }

            $scope.isDaemon=function(){
                return configDomainDef.type == 'daemon';
            }
            $scope.add=function(){
                $state.go(".add");
            }
    }]);//end of apps-admin-config-list-ctrl

    appsConfigModule.controller("apps-admin-config-list-add-ctrl",['$scope',
        'configDomainDef','$state','$uibModal','$uibModalInstance',
        'DaemonConfigItem','SharedConfigItem',
        function($scope,configDomainDef,$state,$uibModal,$uibModalInstance,DaemonConfigItem,SharedConfigItem){
            $scope.close=function(){
                $uibModalInstance.close();
            }
            $scope.dismiss=function(){
                $uibModalInstance.dismiss();
            }
            $scope.error=function(){
                window.alert("Error occurs");
            };
            $scope.add=function(){
                if(configDomainDef.type=='daemon'){
                    DaemonConfigItem.add(
                        {
                            'domain':configDomainDef.domain,
                            'uid':configDomainDef.uuid,
                            'key':$scope.key
                        },
                        $scope.value,
                        $scope.close,
                        $scope.error
                    );
                }
                else{
                    SharedConfigItem.add(
                        {
                            'domain':configDomainDef.domain,
                            'key':$scope.key
                        },
                        $scope.value,
                        $scope.close,
                        $scope.error
                    );
                }
            }

        }
    ]);
});