'use strict';

define(['angular','angular-route','angular-animate','apps-admin-resource','ui-bootstrap-tpls'],function(angular){
    var appsAdminModule = angular.module('apps-admin',['apps-admin-resource','ngResource','ngRoute','ui.bootstrap.modal',
        'template/modal/backdrop.html',
        'template/modal/window.html'
        ]
    );
    appsAdminModule.controller('apps-admin-ctrl',['$scope','$modal',
            'DaemonsListService','DaemonInfoService','DaemonStatusService',
            'DaemonWebServersListService','DaemonWebServerStatusService',
            function($scope,$modal,DaemonsListService,DaemonInfoService,DaemonStatusService,DaemonWebServersListService,DaemonWebServerStatusService){
                var WebServer = function(daemonUid,serverInfo){
                    var self=this;
                    for(var attr in serverInfo){
                        this[attr] = serverInfo[attr];
                    }
                    this.isStarted=function(){return this.status.toLowerCase()=="started" || this.status.toLowerCase()=="running" ;};
                    this.isStopped=function(){return this.status.toLowerCase()=="stopped";};
                    this.start=function(){
                        DaemonWebServerStatusService.put({uid:daemonUid,wid:this.name},{action:"START"},function(statusResult){
                            self.status=statusResult.status;
                        });
                    };
                    this.stop=function(){
                        DaemonWebServerStatusService.put({uid:daemonUid,wid:this.name},{action:"STOP"},function(statusResult){
                            self.status=statusResult.status;
                        });
                    };
                    this.restart=function(){
                        DaemonWebServerStatusService.put({uid:daemonUid,wid:this.name},{action:"RESTART"},function(statusResult){
                            self.status=statusResult.status;
                        });
                    };

                    return this;
                };

                var Daemon = function(daemonInfo){
                    var self=this;
                    this.webServers=[];
                    DaemonWebServersListService.get({uid:daemonInfo.uuid},function(data){
                        for(var webServerPos=0;webServerPos<data.length;++webServerPos){
                            self.webServers.push(new WebServer(self.uuid,data[webServerPos].toJSON()));
                        }
                    });
                    for(var attr in daemonInfo){
                        if(attr=="webServerList") continue;
                        this[attr] = daemonInfo[attr];
                    }

                    this.isStarted=function(){return self.status.toLowerCase()=="started";};
                    this.isStopped=function(){return self.status.toLowerCase()=="stopped";};
                    this.isOtherStatus=function(){return !self.isStarted() && !self.isStopped();};

                    this.viewDetails = function(){$modal.open({
                                 animation: true,
                                 templateUrl: requirejs.toUrl("apps-admin-daemon-details.html"),
                                 controller: 'apps-admin-daemon-details-ctrl',
                                 size: 'lg',
                                 resolve: {
                                   daemonInfo: function () {
                                     return self;
                                   }
                                 }
                               })};
                    this.start=function(){
                        DaemonStatusService.put({uid:this.uuid},{action:"START"},function(statusResult){
                            self.status=statusResult.status;
                        });
                    }
                    this.halt=function(){
                        DaemonStatusService.put({uid:this.uuid},{action:"HALT"},function(statusResult){
                            self.status=statusResult.status;
                        });
                    }
                    this.stop=function(){
                        DaemonStatusService.put({uid:this.uuid},{action:"STOP"},function(statusResult){
                            self.status=statusResult.status;
                        });
                    }
                    return this;
                }
                $scope.refresh=function(){
                    DaemonsListService.get(function(data){
                        $scope.daemons = [];
                        var daemonPos=0;
                        for(var daemonPos=0;daemonPos<data.length;++daemonPos){
                            DaemonInfoService.get({uid:data[daemonPos].uuid},function(daemonInfo){
                                $scope.daemons.push(new Daemon(daemonInfo.toJSON()));
                            })
                        }
                    });
                }
                $scope.refresh();
            }]
        )
        .config(['$routeProvider',function($routeProvider) {
          $routeProvider
           .when('/', {
            templateUrl: requirejs.toUrl('apps-admin-view.html'),
            controller: 'apps-admin-ctrl'
          })}]);

    appsAdminModule.controller('apps-admin-daemon-details-ctrl',['$scope','$modalInstance','daemonInfo',
                function($scope,$modalInstance,daemonInfo){
                    $scope.daemonInfo = daemonInfo;
                    $scope.ok=function(){
                        $modalInstance.dismiss('ok');
                    }
                }]
            );

}
);