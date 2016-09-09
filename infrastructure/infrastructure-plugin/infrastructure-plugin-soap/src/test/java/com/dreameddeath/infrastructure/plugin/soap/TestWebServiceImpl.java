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

package com.dreameddeath.infrastructure.plugin.soap;

import com.dreameddeath.core.service.annotation.ServiceDef;
import com.dreameddeath.core.service.soap.AbstractSoapExposableService;
import com.dreameddeath.interfaces.test.v0.data.TestResponse;
import com.dreameddeath.interfaces.test.v0.data.in.TestRequest;
import com.dreameddeath.interfaces.test.v0.message.TestFault;
import com.dreameddeath.interfaces.test.v0.message.TestWebService;

import javax.jws.WebParam;

/**
 * Created by Christophe Jeunesse on 09/09/2016.
 */
@ServiceDef(domain = "test",name = "soapTest",version = "1.0")
public class TestWebServiceImpl extends AbstractSoapExposableService implements TestWebService {
    @Override
    public TestResponse testOperation(@WebParam(partName = "parameters", name = "TestRequest", targetNamespace = "http://www.dreameddeath.com/Interfaces/test/v0/data/in") TestRequest parameters) throws TestFault {
        TestResponse response = new TestResponse();
        response.setName(parameters.getName());
        response.setGreeting("Hello "+parameters.getName());
        return response;
    }
}
