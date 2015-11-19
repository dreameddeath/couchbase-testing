'use strict';

define(['angular','angular-route','angular-animate','apps-admin-config-resource','ui-bootstrap-tpls','angular-ui-router'],function(angular){
    var appsConfigModule = angular.module('apps-admin-config-list',
        ['apps-admin-config-resource','ngResource','ngRoute','ui.router'
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
                        self.value=value;
                        self.newValue=null;
                    }
                    if(self.type=='daemon'){
                        var queryParamDef = {
                            uid:self.domainDef.uuid,
                            domain:self.domainDef.domain,
                            key:self.name
                        };
                        this.save=function(){
                            DaemonConfigItem.put(queryParamDef,self.newValue,self.updateValue);
                        };
                        this.refresh=function(){
                            DaemonConfigItem.get(queryParamDef,self.updateValue);
                        }
                    }
                    else{
                        var queryParamDef = {
                            domain:self.domainDef.domain,
                            key:self.name
                        };
                        this.save=function(){
                            SharedConfigItem.put(queryParamDef,self.newValue,self.updateValue);
                        };
                        this.refresh=function(){
                            SharedConfigItem.get(queryParamDef,self.updateValue);
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
                for(var key of $scope.configEntries){
                    if($scope.configEntries[key].isModified()){
                        result[key]=$scope.configEntries[key].newValue;
                    }
                }
                return result;
            }
            $scope.partialRefresh=function(newValuesRaw){
                 var newValues = newValuesRaw.toJSON();
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
            }
            $scope.fullRefresh=function(newValues){
                $scope.configEntries={};
                $scope.partialRefresh(newValues);
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
                    SharedConfigList.put(queryParamDef,updateRequest,function(){
                            this.partialRefresh(updateRequest);
                        }
                    );
                };
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
                        DaemonConfigList.put(queryParamDef,updateRequest,function(){
                                this.partialRefresh(updateRequest);
                            }
                    );
                };
            }
            $scope.refresh();

            $scope.closeConfig=function(){
                $state.go('^');
            }
    }]);//end of apps-admin-config-list-ctrl


});