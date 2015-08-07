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

package com.dreameddeath.core.helper.service;

import com.dreameddeath.core.service.model.AbstractExposableService;
import com.dreameddeath.core.session.impl.CouchbaseSessionFactory;
import com.dreameddeath.core.user.IUserFactory;

/**
 * Created by Christophe Jeunesse on 16/04/2015.
 */
public abstract class AbstractDaoRestService extends AbstractExposableService {
    private CouchbaseSessionFactory _sessionFactory;
    private IUserFactory _userFactory;

    public void setSessionFactory(CouchbaseSessionFactory sessionFactory){
        _sessionFactory = sessionFactory;
    }

    public CouchbaseSessionFactory getSessionFactory() {
        return _sessionFactory;
    }

    public void setUserFactory(IUserFactory userFactory){
        _userFactory = userFactory;
    }

    public IUserFactory getUserFactory() {
        return _userFactory;
    }
}
