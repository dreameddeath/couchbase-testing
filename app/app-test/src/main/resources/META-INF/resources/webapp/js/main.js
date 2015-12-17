requirejs(['require','angular','apps-admin','angular-js-css-file'], function (require,angular) {
    'use strict';

    /*
     * place operations that need to initialize prior to app start here
     * using the `run` function on the top-level module
     */

    require(['domReady!'], function (document) {
        angular.bootstrap(document, ['apps-admin']);
    });
});