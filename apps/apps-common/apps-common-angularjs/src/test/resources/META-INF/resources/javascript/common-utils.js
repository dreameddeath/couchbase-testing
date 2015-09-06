"use strict";

function loadFromClassPath(pathName,filename){
    load({
        script:org.apache.commons.io.IOUtils.toString(new java.io.InputStreamReader(java.lang.Thread.currentThread().getContextClassLoader().getResourceAsStream(pathName))),
        name:filename||pathName
       }
    )
}