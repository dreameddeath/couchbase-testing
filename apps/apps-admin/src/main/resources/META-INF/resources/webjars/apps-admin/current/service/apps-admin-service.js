'use strict';

define(['angular','angular-route','angular-animate','apps-admin-service-resource','ui-bootstrap-tpls'],function(angular){
    var appsAdminModule = angular.module('apps-admin-service',['apps-admin-service-resource','ngResource','ngRoute','ui.bootstrap.modal',
        'template/modal/backdrop.html',
        'template/modal/window.html'
        ]
    );
    appsAdminModule.controller('apps-admin-service-ctrl',['$scope','$modal',
            'DaemonsListService','DaemonInfoService','DaemonStatusService',
            'DaemonWebServersListService','DaemonWebServerStatusService',
            function($scope,$modal,DaemonsListService,DaemonInfoService,DaemonStatusService,DaemonWebServersListService,DaemonWebServerStatusService){
                var WebServer = function(daemonUid,serverInfo){
                    var self=this;
                    for(var attr in serverInfo){
                        this[attr] = serverInfo[attr];
                    }
                    this.type = this['className'].substring(this['className'].lastIndexOf('.')+1);
                    this.isStarted=function(){return this.status.toLowerCase()=="started" || this.status.toLowerCase()=="running" ;};
                    this.isStopped=function(){return this.status.toLowerCase()=="stopped";};
                    this.updateFromStatusResponse=function(statusResponse){
                        this.address = statusResponse.address;
                        this.status = statusResponse.status;
                        this.port = statusResponse.port;
                    };
                    this.start=function(){
                        DaemonWebServerStatusService.put({uid:daemonUid,wid:this.name},{action:"START"},function(statusResult){
                            self.updateFromStatusResponse(statusResult);
                        });
                    };
                    this.stop=function(){
                        DaemonWebServerStatusService.put({uid:daemonUid,wid:this.name},{action:"STOP"},function(statusResult){
                            self.updateFromStatusResponse(statusResult);
                        });
                    };
                    this.restart=function(){
                        DaemonWebServerStatusService.put({uid:daemonUid,wid:this.name},{action:"RESTART"},function(statusResult){
                            self.updateFromStatusResponse(statusResult);
                        });
                    };
                    this.refreshStatus=function(){
                        DaemonWebServerStatusService.get({uid:daemonUid,wid:this.name},function(statusResult){
                            self.updateFromStatusResponse(statusResult);
                        });
                    }
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
                    this.type = this['className'].substring(this['className'].lastIndexOf('.')+1);

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
                    this.refreshWebServers=function(){
                        for(var serverId=0;serverId<this.webServers.length;++serverId){
                            this.webServers[serverId].refreshStatus();
                        }
                    }
                    this.refreshStatusFromResponse=function(statusResponse){
                        this.status=statusResponse.status;
                        this.refreshWebServers()
                    };
                    this.start=function(){
                        DaemonStatusService.put({uid:this.uuid},{action:"START"},function(statusResult){
                            self.refreshStatusFromResponse(statusResult);
                        });
                    }
                    this.halt=function(){
                        DaemonStatusService.put({uid:this.uuid},{action:"HALT"},function(statusResult){
                            self.refreshStatusFromResponse(statusResult);
                        });
                    }
                    this.stop=function(){
                        DaemonStatusService.put({uid:this.uuid},{action:"STOP"},function(statusResult){
                            self.refreshStatusFromResponse(statusResult);
                        });
                    }
                    this.refreshStatus=function(){
                        DaemonStatusService.get({uid:this.uuid},function(statusResult){
                            self.refreshStatusFromResponse(statusResult);
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
        );

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