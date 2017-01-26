'use strict';

define(['angular','angular-route','angular-animate','apps-admin-dao-resource','ui-bootstrap-tpls'],function(angular){
    var appsDaosModule = angular.module('apps-admin-dao',[
            'apps-admin-dao-resource','ngResource','ngRoute'
        ]
    );

    appsDaosModule.config(['$stateProvider', function($stateProvider) {
                    $stateProvider
                    .state('admin.dao', {
                       url:         '/daos',
                       templateUrl: requirejs.toUrl('apps-admin-dao.html')/*,
                       controller:"apps-admin-dao-ctrl"*/
                    })
                    .state('admin.dao.details', {
                       url:         '/{uid}',
                       templateUrl: requirejs.toUrl('apps-admin-dao-details.html'),
                       controller:"apps-admin-dao-details-ctrl"
                    })
    }]);

    appsDaosModule.controller('apps-admin-dao-ctrl',['$scope','$state',
            'DaoListService','DaoInfoService',
            function($scope,$state,DaoListService,DaoInfoService){
                var DaoInstanceInfo = function(daoInfo){
                    var self=this;
                    for(var attr in daoInfo){
                        this[attr] = daoInfo[attr];
                    }
                    this.type = this.className.substring(this.className.lastIndexOf('.')+1);
                    this.mainEntityStr = this.mainEntity.modelId.domain+'/'+this.mainEntity.modelId.name;
                    this.entities = [];
                    this.entities.push(this.mainEntity);
                    if(this.childEntities!=null){
                        this.childEntities.forEach(function(v){self.entities.push(v)},this);
                    }

                    this.viewDetails = function(){
                        $state.go('admin.dao.details',{uid:self.uuid});
                    };

                    return this;
                }

                $scope.setCurrUid=function(uid){
                    $scope.currDaoId=uid;
                    $scope.updateCurrVersion();
                }

                $scope.updateCurrVersion=function(){
                    if($scope.currDaoId==null){
                        $scope.currDao=null;
                    }
                    else{
                        $scope.currDao=$scope.daosPerUid[$scope.currDaoId];
                    }
                }
                $scope.$state = $state;
                $scope.refresh=function(){
                    DaoListService.get(function(data){
                        $scope.daos = [];
                        $scope.daosPerUid = {};
                        var daoPos=0;
                        for(var daoPos=0;daoPos<data.length;++daoPos){
                            var daoInfo=new DaoInstanceInfo(data[daoPos].toJSON())
                            $scope.daos.push(daoInfo);
                            $scope.daosPerUid[daoInfo.uuid]=daoInfo;
                        }
                        $scope.updateCurrVersion();
                    });
                }
                $scope.currDaoId=null;
                $scope.refresh();

                $scope.close=function(){
                    $state.go("^");
                }
            }]
        );


    appsDaosModule.controller('apps-admin-dao-details-ctrl',['$scope','$state','$stateParams',
                function($scope,$state,$stateParams){
                    $scope.$parent.setCurrUid($stateParams.uid);
                }]
            );

}
);