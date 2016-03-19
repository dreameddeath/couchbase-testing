'use strict';

define(['angular','angular-route','angular-animate','apps-admin-process-resource','ui-bootstrap-tpls'],function(angular){
    var appsProcessorsModule = angular.module('apps-admin-process',['apps-admin-process-resource','ngResource','ngRoute','ui.bootstrap.modal',
        'template/modal/backdrop.html',
        'template/modal/window.html'
        ]
    );

    appsProcessorsModule.config(['$stateProvider', function($stateProvider) {
                    $stateProvider.state('admin.process', {
                       url:         '/processors',
                       templateUrl: requirejs.toUrl('apps-admin-process.html')
                    })
    }]);

    appsProcessorsModule.controller('apps-admin-process-ctrl',['$scope','$uibModal',
            'TasksListService','TasksInfoService','JobsListService','JobsInfoService',
            function($scope,$uibModal,TasksListService,TasksInfoService,JobsListService,JobsInfoService){

                var ProcessorInstanceInfo = function(type,processorInfo){
                    var self=this;
                    for(var attr in processorInfo){
                        this[attr] = processorInfo[attr];
                    }
                    this.type = type;
                    this.name = this.processingName;
                    this.version = this.processingVersion;
                    this.versionState = this.processingVersionState;
                    this.mainEntityStr = this.entity.modelId.domain+'/'+this.entity.modelId.name;

                    this.viewDetails = function(){$uibModal.open({
                                 animation: true,
                                 templateUrl: requirejs.toUrl("apps-admin-process-details.html"),
                                 controller: 'apps-admin-process-details-ctrl',
                                 size: 'lg',
                                 resolve: {
                                   processorInfo: function () {
                                     return self;
                                   }
                                 }
                               })};

                    return this;
                }

                $scope.buildProcessorsMap=function(domain,data){
                    var processorsMap = {};
                    for(var processorPos=0;processorPos<data.length;++processorPos){
                        var processorInstanceInfo = new ProcessorInstanceInfo(domain,data[processorPos].toJSON());
                        if(processorsMap[processorInstanceInfo.name]==null){
                            processorsMap[processorInstanceInfo.name]={
                                name:processorInstanceInfo.name,
                                versions:{}
                            };
                        }
                        if(processorsMap[processorInstanceInfo.name].versions[processorInstanceInfo.version]==null){
                            processorsMap[processorInstanceInfo.name].versions[processorInstanceInfo.version]={
                                version: processorInstanceInfo.version,
                                state: processorInstanceInfo.versionState,
                                entity: processorInstanceInfo.mainEntityStr,
                                clients:[]
                            };
                        }
                        processorsMap[processorInstanceInfo.name].versions[processorInstanceInfo.version].clients.push(processorInstanceInfo);
                    }

                    return processorsMap;
                }
                $scope.jobsRefresh=function(){
                    JobsListService.get(function(data){
                        $scope.jobProcessors=$scope.buildProcessorsMap("job",data);
                    });
                };
                $scope.tasksRefresh=function(){
                    TasksListService.get(function(data){
                        $scope.taskProcessors=$scope.buildProcessorsMap("task",data);
                    });
                }
                $scope.jobsRefresh();
                $scope.tasksRefresh();
            }]
        );

    appsProcessorsModule.controller('apps-admin-process-details-ctrl',['$scope','$uibModalInstance','processorInfo',
                function($scope,$uibModalInstance,processorInfo){
                    $scope.processorInfo = processorInfo;
                    $scope.ok=function(){
                        $uibModalInstance.close();
                    }
                }]
            );

}
);