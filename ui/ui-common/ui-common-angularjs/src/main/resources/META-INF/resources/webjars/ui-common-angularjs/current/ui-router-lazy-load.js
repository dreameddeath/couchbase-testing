define(['angular','ocLazyLoad','ui-router-extras'],
function(angular){
    var uiRouterLazyLoadModule = angular.module('ui-router-lazy-load',
            ['ui.router',
             'ct.ui.router.extras',
             'oc.lazyLoad']
        ).config([
            '$ocLazyLoadProvider',
            '$futureStateProvider',
           function($ocLazyLoadProvider,
                    $futureStateProvider) {

               $ocLazyLoadProvider.config ({
                   debug: false,
                   jsLoader: requirejs,
                   loadedModules: ['ui-router-lazy-load']
               });

               var ocLazyLoadStateFactory = function ($q, $ocLazyLoad, futureState) {
                   var deferred = $q.defer();
                   //var module=require(futureState.moduleDef);
                   $ocLazyLoad.load(futureState.moduleDef).then(function(name) {
                       deferred.resolve();
                   }, function() {
                       deferred.reject();
                   });
                   return deferred.promise;
               };

               $futureStateProvider.stateFactory('ocLazyLoad', ocLazyLoadStateFactory);
           }]);

});