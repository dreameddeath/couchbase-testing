'use strict';

define(['angular','angular-route','angular-animate','apps-admin-process-resource','ui-bootstrap-tpls'],function(angular){
    var appsProcessorsModule = angular.module('apps-admin-process',['apps-admin-process-resource','ngResource','ngRoute','ui.bootstrap.modal',
        //'template/modal/backdrop.html',
        'uib/template/modal/window.html'
        ]
    );

    appsProcessorsModule.config(['$stateProvider', function($stateProvider) {
                    $stateProvider.state('admin.process', {
                       url:         '/processors',
                       templateUrl: requirejs.toUrl('apps-admin-process.html')
                    })
                    .state('admin.process.detail', {
                        url:         '/{type}/{name}/{version}',
                        templateUrl: requirejs.toUrl('apps-admin-process-detail.html'),
                        controller:"apps-admin-process-details-ctrl"
                    })

    }]);

    appsProcessorsModule.controller('apps-admin-process-ctrl',['$scope','$state',
            'TasksListService','TasksInfoService','JobsListService','JobsInfoService',
            function($scope,$state,TasksListService,TasksInfoService,JobsListService,JobsInfoService){
                var ProcessorInstanceInfo = function(type,processorInfo){
                    var self=this;
                    for(var attr in processorInfo){
                        this[attr] = processorInfo[attr];
                    }
                    this.type = type;
                    this.domain = this.processingDomain;
                    this.name = this.processingName;
                    this.className = this.processingService;
                    this.version = this.processingVersion;
                    this.versionState = this.processingVersionState;
                    this.mainEntityStr = this.entity.modelId.domain+'/'+this.entity.modelId.name;
                    return this;
                }

                $scope.currVersionName={};
                $scope.currProcessorInfo={};
                $scope.$state = $state;
                $scope.setCurrVersion=function(type,name,version){
                    $scope.currVersionName={"type":type,"name":name,"version":version};
                    $scope.updateCurrVersion();
                }
                $scope.updateCurrVersion=function(){
                    var refMap=null;
                    if($scope.currVersionName.type==='job'){
                        refMap=$scope.jobProcessors;
                    }
                    else if($scope.currVersionName.type==='task'){
                        refMap=$scope.taskProcessors;
                    }
                    $scope.currProcessorInfo=(refMap!=null)?refMap[$scope.currVersionName.name].versions[$scope.currVersionName.version]:{};
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
                                type:processorInstanceInfo.type,
                                domain:processorInstanceInfo.domain,
                                name:processorInstanceInfo.name,
                                className:processorInstanceInfo.className,
                                version:processorInstanceInfo.version,
                                state:processorInstanceInfo.versionState,
                                mainEntityStr:processorInstanceInfo.mainEntityStr,
                                showDetails:function(){
                                    var self=this;
                                    $state.go("admin.process.detail",{"type":self.type,"name":self.name,"version":self.version});
                                },
                                instances:[]
                            };
                        }
                        processorsMap[processorInstanceInfo.name].versions[processorInstanceInfo.version].instances.push(processorInstanceInfo);
                    }

                    return processorsMap;
                }

                $scope.jobsRefresh=function(){
                    JobsListService.get(function(data){
                        $scope.jobProcessors=$scope.buildProcessorsMap("job",data);
                        $scope.updateCurrVersion();
                    });
                };
                $scope.tasksRefresh=function(){
                    TasksListService.get(function(data){
                        $scope.taskProcessors=$scope.buildProcessorsMap("task",data);
                        $scope.updateCurrVersion();
                    });
                }

                $scope.jobsRefresh();
                $scope.tasksRefresh();
                $scope.close=function(){
                    $state.go("^");
                };
            }]
        );

    appsProcessorsModule.controller('apps-admin-process-details-ctrl',['$scope','$state','$stateParams',
                function($scope,$state,$stateParams){
                    $scope.$parent.setCurrVersion($stateParams.type,$stateParams.name,$stateParams.version);
                    $scope.ok=function(){
                        $uibModalInstance.close();
                    }
                }]
            );

}
);