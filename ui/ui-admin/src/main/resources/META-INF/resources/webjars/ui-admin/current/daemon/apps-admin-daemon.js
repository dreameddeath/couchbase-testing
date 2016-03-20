'use strict';

define(['angular','angular-route','angular-animate','apps-admin-daemon-resource','apps-admin-config-list','angular-ui-router','ui-bootstrap-tpls'],function(angular){
    var appsAdminModule = angular.module('apps-admin-daemon',['apps-admin-daemon-resource',
        'apps-admin-config-list','ngResource','ngRoute','ui.router'
        ]
    );

    appsAdminModule.config(['$stateProvider', function($stateProvider) {
        $stateProvider
        .state('admin.daemon', {
            url:         '/daemons',
            templateUrl: requirejs.toUrl('apps-admin-daemon.html')
         })
        .state('admin.daemon.list', {
            url:         '/list',
            templateUrl: requirejs.toUrl('apps-admin-daemon-list.html'),
        })
        .state('admin.daemon.detail', {
            url:         '/detail/:uid',
            templateUrl: requirejs.toUrl('apps-admin-daemon-detail.html'),
        })
        .state('admin.daemon.detail.config', {
            url:         '/config/:domain',
            templateUrl: requirejs.toUrl('apps-admin-config-list.html'),
            resolve :{
                configDomainDef:function($stateParams){
                    return {
                        'type':'daemon',
                        'uuid':$stateParams.uid,
                        'domain':$stateParams.domain
                    };
                }
            } ,
            controller:"apps-admin-config-list-ctrl"
        })
        .state('admin.daemon.detail.config.add', {
            url:        '/add',
            onEnter: ['AppsAdminConfigListAddModal','$state','$stateParams',
            function(AppsAdminConfigListAddModal,$state,$stateParams){
                    AppsAdminConfigListAddModal.open($state,{
                            'type':'daemon',
                            'uuid':$stateParams.uid,
                            'domain':$stateParams.domain
                    });
                }
            ]
        })
    }]);

    appsAdminModule.factory('WebServerBuilder', ['DaemonWebServerStatusService',
            function(DaemonWebServerStatusService){
                return function(parentUid,infos){
                    var WebServer=function(daemonUid,webServerInfo){
                        var self=this;
                        for(var attr in webServerInfo){
                            this[attr] = webServerInfo[attr];
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

                    return new WebServer(parentUid,infos);
                }
            }
        ] );

        appsAdminModule.factory('DaemonBuilder',
            ['$state','DaemonWebServersListService','DaemonStatusService','WebServerBuilder',
            function($state,DaemonWebServersListService,DaemonStatusService,WebServerBuilder){
                return function(infos){
                    var DaemonInfo = function(daemonInfo){
                        var self=this;
                        this.webServers=[];
                        DaemonWebServersListService.get({uid:daemonInfo.uuid},function(data){
                            for(var webServerPos=0;webServerPos<data.length;++webServerPos){
                                self.webServers.push(WebServerBuilder(self.uuid,data[webServerPos].toJSON()));
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

                        this.viewDetails = function(){
                            $state.go("admin.daemon.detail",{"uid":self.uuid})
                        };
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
                    };

                    return new DaemonInfo(infos);
                }
            }
        ]);


    appsAdminModule.controller('apps-admin-daemon-ctrl',['$scope','$state','$stateParams',
        function($scope,$state,$stateParams){
            if($state.is("admin.daemon")){
                $state.go("admin.daemon.list");
            }
        }
    ]);
    appsAdminModule.controller('apps-admin-daemon-list-ctrl',['$scope','$state',
            'DaemonsListService','DaemonBuilder','DaemonInfoService',
            function($scope,$state,DaemonsListService,DaemonBuilder,DaemonInfoService){
                $scope.refresh=function(){
                    DaemonsListService.get(function(data){
                        $scope.daemons = [];
                        var daemonPos=0;
                        for(var daemonPos=0;daemonPos<data.length;++daemonPos){
                            DaemonInfoService.get({uid:data[daemonPos].uuid},function(daemonInfo){
                                $scope.daemons.push(DaemonBuilder(daemonInfo.toJSON()));
                            })
                        }
                    });
                }
                $scope.refresh();
            }]
        );

    appsAdminModule.controller('apps-admin-daemon-detail-ctrl',['$scope','$state','$stateParams','DaemonInfoService','DaemonBuilder',
            function($scope,$state,$stateParams,DaemonInfoService,DaemonBuilder){
                DaemonInfoService.get({uid:$stateParams.uid},function(daemonInfo){
                    $scope.daemonInfo=DaemonBuilder(daemonInfo);
                    if($state.data==null){
                        $state.data={};
                    }
                    $state.data.daemonInfo = daemonInfo;
                });

                $scope.close=function(){
                    $state.go('^.list');
                };

                $scope.isCurrent=function(){
                    return $state.is("admin.daemon.detail");
                }
                $scope.viewConfig = function(domain){
                        $state.go(".config",{"domain":domain});
                };
            }]
    );

}
);