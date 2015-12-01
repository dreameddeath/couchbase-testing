'use strict';
define(['angular'],function(angular){
angular.module("angular-js-css-file",[]).directive("ngCssFiles",function(){
            return {
                restrict : 'A',
                link: function(scope, element, attributes){
                     var cssFiles = scope.$eval(attributes.ngCssFiles);
                     if(typeof value === 'string'){
                        cssFiles=[cssFiles];
                     }
                     for(var posCss=0;posCss<cssFiles.length;++posCss){
                        var link=document.createElement("link");
                         link.rel = "stylesheet";
                         link.href = requirejs.toUrl(cssFiles[posCss]);
                         link.type = "text/css";
                         var head = document.getElementsByTagName("head")[0];
                         var linkList = head.getElementsByTagName("link");
                         for(var linkPos=0;linkPos<linkList.length;++linkPos){
                            if(linkList[linkPos].href==link.href){
                                link=null;
                                break;
                            }
                         }
                         if(link!=null){
                            document.getElementsByTagName("head")[0].appendChild(link);
                         }
                     }
                }
            };
        });


})