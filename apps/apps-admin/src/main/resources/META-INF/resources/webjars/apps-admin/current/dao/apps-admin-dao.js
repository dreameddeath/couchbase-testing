'use strict';

define(['angular','angular-route','angular-animate','apps-admin-dao-resource','ui-bootstrap-tpls'],function(angular){
    var appsDaosModule = angular.module('apps-admin-dao',['apps-admin-dao-resource','ngResource','ngRoute','ui.bootstrap.modal',
        'template/modal/backdrop.html',
        'template/modal/window.html'
        ]
    );
    appsDaosModule.controller('apps-admin-dao-ctrl',['$scope','$uibModal',
            'DaoListService','DaoInfoService',
            function($scope,$uibModal,DaoListService,DaoInfoService){
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

                    this.viewDetails = function(){$uibModal.open({
                                 animation: true,
                                 templateUrl: requirejs.toUrl("apps-admin-dao-details.html"),
                                 controller: 'apps-admin-dao-details-ctrl',
                                 size: 'lg',
                                 resolve: {
                                   daoInfo: function () {
                                     return self;
                                   }
                                 }
                               })};

                    return this;
                }
                $scope.refresh=function(){
                    DaoListService.get(function(data){
                        $scope.daos = [];
                        var daoPos=0;
                        for(var daoPos=0;daoPos<data.length;++daoPos){
                            $scope.daos.push(new DaoInstanceInfo(data[daoPos].toJSON()));
                        }
                    });
                }
                $scope.refresh();
            }]
        );

    appsDaosModule.controller('apps-admin-dao-details-ctrl',['$scope','$uibModalInstance','daoInfo',
                function($scope,$uibModalInstance,daoInfo){
                    $scope.dao = daoInfo;
                    $scope.ok=function(){
                        $uibModalInstance.dismiss('ok');
                    }
                }]
            );

}
);