requirejs.config({
    paths: {
        'domReady': webjars.path('apps-admin', 'domReady'),
        'apps-admin': webjars.path('apps-admin', 'apps-admin'),
        'apps-admin-daemon-details': webjars.path('apps-admin', 'apps-admin-daemon-details'),
        'apps-admin-resource': webjars.path('apps-admin', 'apps-admin-resource'),
        'apps-admin.view': webjars.path('apps-admin', 'apps-admin.view'),
        'ocLazyLoad': webjars.path('apps-admin', "ocLazyLoad.require")
    },
    shim: {
        "ocLazyLoad": "ocLazyLoad.require"
    }
});
