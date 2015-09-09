"use strict";

function loadFromClassPath(pathName,filename){
    load({
        script:org.apache.commons.io.IOUtils.toString(new java.io.InputStreamReader(java.lang.Thread.currentThread().getContextClassLoader().getResourceAsStream(pathName))),
        name:filename||pathName
       }
    )
};

var Timer    = Java.type("java.util.Timer");

function setTimerRequest(handler, delay, interval, args) {
    handler = handler || function() {};
    delay = delay || 0;
    interval = interval || 0;

    var applyHandler = function() handler.apply(this, args);
    var timer = new Timer("setTimerRequest", true);

    if (interval > 0) {
        timer.schedule(applyHandler, delay, interval);
    } else {
        timer.schedule(applyHandler, delay);
    }

    return timer;
}

function clearTimerRequest(timer) {
    timer.cancel();
}

function setInterval() {
    var args = Array.prototype.slice.call(arguments);
    var handler = args.shift();
    var ms = args.shift();

    return setTimerRequest(handler, ms, ms, args);
}

function clearInterval(timer) {
    clearTimerRequest(timer);
}

function setTimeout() {
    var args = Array.prototype.slice.call(arguments);
    var handler = args.shift();
    var ms = args.shift();

    return setTimerRequest(handler, ms, 0, args);
}

function clearTimeout(timer) {
    clearTimerRequest(timer);
}

function setImmediate() {
    var args = Array.prototype.slice.call(arguments);
    var handler = args.shift();

    return setTimerRequest(handler, 0, 0, args);
}

function clearImmediate(timer) {
    clearTimerRequest(timer);
}