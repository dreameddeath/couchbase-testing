"use strict";
var window = domino.createWindow('<!doctype html>'+
'<html ng-app="testApp">'+
'<head><title>Testing</title></head>'+
'<body>'+
'<div ng-controller="testCtrl">'+
'<label>New v2 Testing Name:</label>'+
'<input type="text" ng-model="yourName" placeholder="Enter a name here" ng-change="changeName()">'+
'<hr>'+
'<h1>Hello {{yourName}}!</h1>'+
'<h1>test resource : {{toto}}</h1>'+
'</div>'+
'</body>'+
'</html>','http://localhost:8080/index.html');
var document = window.document;
window.XMLHttpRequest = XMLHttpRequest;
defaultUrlRoot = 'http://localhost:8080/';
window.setTimeout = setTimeout;