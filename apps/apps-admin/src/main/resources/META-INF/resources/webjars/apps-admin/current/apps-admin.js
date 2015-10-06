'use strict';

define(['angular','angular-route','angular-animate','apps-admin-resource','ui-bootstrap-tpls'],function(angular){
    var appsAdminModule = angular.module('apps-admin',['apps-admin-resource','ngResource','ngRoute','ui.bootstrap.modal',
        'template/modal/backdrop.html',
        'template/modal/window.html'
        ]
    );
    appsAdminModule.controller('apps-admin-ctrl',['$scope','DaemonsListService','DaemonInfoService','$modal',
            function($scope,DaemonsListService,DaemonInfoService,$modal){
                $scope.refresh=function(){
                    DaemonsListService.get(function(data){
                        $scope.daemons = [];
                        var daemonPos=0;
                        for(var daemonPos=0;daemonPos<data.length;++daemonPos){
                            DaemonInfoService.get({uid:data[daemonPos].uuid},function(serverInfo){
                                serverInfo.open = function(){$modal.open({
                                     animation: true,
                                     templateUrl: requirejs.toUrl("apps-admin-daemon-details.html"),
                                     controller: 'apps-admin-daemon-details-ctrl',
                                     size: 'lg',
                                     resolve: {
                                       daemonInfo: function () {
                                         return serverInfo;
                                       }
                                     }
                                   })};
                                $scope.daemons.push(serverInfo);
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