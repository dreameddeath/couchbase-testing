/*
 * Copyright Christophe Jeunesse
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.dreameddeath.infrastructure.plugin.couchbase;

import com.dreameddeath.core.dao.factory.CouchbaseDocumentDaoFactory;
import com.dreameddeath.core.session.impl.CouchbaseSessionFactory;
import com.dreameddeath.infrastructure.daemon.plugin.AbstractWebServerPlugin;
import com.dreameddeath.infrastructure.daemon.plugin.IWebServerPluginBuilder;
import com.dreameddeath.infrastructure.daemon.webserver.AbstractWebServer;
import com.dreameddeath.infrastructure.plugin.couchbase.lifecycle.CouchbaseWebServerLifeCycle;
import com.google.common.base.Preconditions;
import org.eclipse.jetty.servlet.ServletContextHandler;

/**
 * Created by Christophe Jeunesse on 20/12/2015.
 */
public class CouchbaseWebServerPlugin extends AbstractWebServerPlugin {
    public static final String GLOBAL_COUCHBASE_DAO_FACTORY_PARAM_NAME = "couchbaseDaoFactory";
    public static final String GLOBAL_COUCHBASE_SESSION_FACTORY_PARAM_NAME = "couchbaseSessionFactory";

    private final CouchbaseDaemonPlugin parentDaemonPlugin;
    private final CouchbaseDocumentDaoFactory documentDaoFactory;
    private final CouchbaseSessionFactory sessionFactory;

    public CouchbaseWebServerPlugin(AbstractWebServer parentServer,Builder builder){
        super(parentServer);
        parentDaemonPlugin=getParentDaemon().getPlugin(CouchbaseDaemonPlugin.class);
        Preconditions.checkNotNull(parentDaemonPlugin,"The webserver {} requires that a couchbase plugin must be defined in the parent daemon",parentServer.getName());
        documentDaoFactory=CouchbaseDocumentDaoFactory.builder()
                .withDaemonUid(getParentDaemon().getUuid().toString())
                .withWebServerUid(getParentWebServer().getUuid().toString())
                .withBucketFactory(parentDaemonPlugin.getBucketFactory())
                .withCuratorFramework(getParentDaemon().getCuratorClient())
                .build();

        sessionFactory=CouchbaseSessionFactory.builder().withDocumentDaoFactory(documentDaoFactory).build();
        getParentWebServer().getLifeCycle().addLifeCycleListener(new CouchbaseWebServerLifeCycle(this));
    }


    public CouchbaseDocumentDaoFactory getDocumentDaoFactory() {
        return documentDaoFactory;
    }

    public CouchbaseSessionFactory getSessionFactory() {
        return sessionFactory;
    }

    @Override
    public void enrich(ServletContextHandler handler) {
        super.enrich(handler);
        handler.setAttribute(GLOBAL_COUCHBASE_DAO_FACTORY_PARAM_NAME,documentDaoFactory);
        handler.setAttribute(GLOBAL_COUCHBASE_SESSION_FACTORY_PARAM_NAME,sessionFactory);
    }



    public static Builder builder(){
        return new Builder();
    }

    public static class Builder implements IWebServerPluginBuilder<CouchbaseWebServerPlugin>{
        @Override
        public CouchbaseWebServerPlugin build(AbstractWebServer parent) {
            return new CouchbaseWebServerPlugin(parent,this);
        }
    }
}
