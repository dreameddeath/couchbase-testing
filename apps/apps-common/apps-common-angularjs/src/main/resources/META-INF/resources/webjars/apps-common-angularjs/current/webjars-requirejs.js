requirejs.config({
    paths: {
        'domReady': webjars.path('apps-common-angularjs', 'domReady'),
        'ocLazyLoad': webjars.path('apps-common-angularjs', "ocLazyLoad.require"),
        'ui-router-tabs' : webjars.path('apps-common-angularjs', "ui-router-tabs")
    },
    shim: {
        "ocLazyLoad": ["angular"],
        "ui-router-tabs" :  ["angular"]
    }
});
