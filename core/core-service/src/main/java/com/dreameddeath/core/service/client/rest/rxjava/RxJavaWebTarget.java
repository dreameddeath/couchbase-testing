/*
 *
 *  * Copyright Christophe Jeunesse
 *  *
 *  *    Licensed under the Apache License, Version 2.0 (the "License");
 *  *    you may not use this file except in compliance with the License.
 *  *    You may obtain a copy of the License at
 *  *
 *  *      http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  *    Unless required by applicable law or agreed to in writing, software
 *  *    distributed under the License is distributed on an "AS IS" BASIS,
 *  *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  *    See the License for the specific language governing permissions and
 *  *    limitations under the License.
 *
 */

package com.dreameddeath.core.service.client.rest.rxjava;

import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Configuration;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriBuilder;
import java.net.URI;
import java.util.Map;

/**
 * Created by Christophe Jeunesse on 17/09/2016.
 */
public class RxJavaWebTarget {
    private final WebTarget origWebTarget;

    public RxJavaWebTarget(WebTarget origWebTarget) {
        this.origWebTarget = origWebTarget;
    }

    public URI getUri() {
        return origWebTarget.getUri();
    }

    public UriBuilder getUriBuilder() {
        return origWebTarget.getUriBuilder();
    }

    public RxJavaWebTarget path(String path) {
        return new RxJavaWebTarget(origWebTarget.path(path));
    }

    public RxJavaWebTarget resolveTemplate(String name, Object value) {
        return new RxJavaWebTarget(origWebTarget.resolveTemplate(name,value));
    }

    public RxJavaWebTarget resolveTemplate(String name, Object value, boolean encodeSlashInPath) {
        return new RxJavaWebTarget(origWebTarget.resolveTemplate(name,value,encodeSlashInPath));
    }

    public RxJavaWebTarget resolveTemplateFromEncoded(String name, Object value) {
        return new RxJavaWebTarget(origWebTarget.resolveTemplateFromEncoded(name,value));
    }

    public RxJavaWebTarget resolveTemplates(Map<String, Object> templateValues) {
        return new RxJavaWebTarget(origWebTarget.resolveTemplates(templateValues));
    }

    public RxJavaWebTarget resolveTemplates(Map<String, Object> templateValues, boolean encodeSlashInPath) {
        return new RxJavaWebTarget(origWebTarget.resolveTemplates(templateValues, encodeSlashInPath));
    }

    public RxJavaWebTarget resolveTemplatesFromEncoded(Map<String, Object> templateValues) {
        return new RxJavaWebTarget(origWebTarget.resolveTemplatesFromEncoded(templateValues));
    }

    public RxJavaWebTarget matrixParam(String name, Object... values) {
        return new RxJavaWebTarget(origWebTarget.matrixParam(name,values));
    }

    public RxJavaWebTarget queryParam(String name, Object... values) {
        return new RxJavaWebTarget(origWebTarget.queryParam(name,values));
    }

    public RxJavaWebTarget property(String name, Object value) {
        return new RxJavaWebTarget(origWebTarget.property(name,value));
    }

    public RxJavaWebTarget register(Class<?> componentClass) {
        return new RxJavaWebTarget(origWebTarget.register(componentClass));
    }

    public RxJavaWebTarget register(Class<?> componentClass, int priority) {
        return new RxJavaWebTarget(origWebTarget.register(componentClass,priority));
    }

    public RxJavaWebTarget register(Class<?> componentClass, Class<?>... contracts) {
        return new RxJavaWebTarget(origWebTarget.register(componentClass, contracts));
    }

    public RxJavaWebTarget register(Class<?> componentClass, Map<Class<?>, Integer> contracts) {
        return new RxJavaWebTarget(origWebTarget.register(componentClass,contracts));
    }

    public RxJavaWebTarget register(Object component) {
        return new RxJavaWebTarget(origWebTarget.register(component));
    }

    public RxJavaWebTarget register(Object component, int priority) {
        return new RxJavaWebTarget(origWebTarget.register(component, priority));
    }

    public RxJavaWebTarget register(Object component, Class<?>... contracts) {
        return new RxJavaWebTarget(origWebTarget.register(component,contracts));
    }

    public RxJavaWebTarget register(Object component, Map<Class<?>, Integer> contracts) {
        return new RxJavaWebTarget(origWebTarget.register(component,contracts));
    }
    
    public RxJavaInvocationBuilder request() {
        return new RxJavaInvocationBuilder(origWebTarget.request());
    }

    public RxJavaInvocationBuilder request(String... acceptedResponseTypes) {
        return new RxJavaInvocationBuilder(origWebTarget.request(acceptedResponseTypes));
    }

    public RxJavaInvocationBuilder request(MediaType... acceptedResponseTypes) {
        return new RxJavaInvocationBuilder(origWebTarget.request(acceptedResponseTypes));
    }

    public Configuration getConfiguration() {
        return origWebTarget.getConfiguration();
    }

   
}
