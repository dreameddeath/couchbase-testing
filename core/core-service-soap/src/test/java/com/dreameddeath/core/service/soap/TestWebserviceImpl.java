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

package com.dreameddeath.core.service.soap;

import com.dreameddeath.core.context.IGlobalContext;
import com.dreameddeath.core.service.annotation.ServiceDef;
import com.dreameddeath.core.service.interceptor.PropertyUtils;
import com.dreameddeath.core.service.soap.testing.SoapTestingServer;
import com.dreameddeath.interfaces.test.v0.data.OffersCCOResp;
import com.dreameddeath.interfaces.test.v0.data.TestResponse;
import com.dreameddeath.interfaces.test.v0.data.in.OffersCCO;
import com.dreameddeath.interfaces.test.v0.data.in.TestRequest;
import com.dreameddeath.interfaces.test.v0.message.TestFault;
import com.dreameddeath.interfaces.test.v0.message.TestWebService;
import com.google.common.base.Preconditions;

import javax.annotation.Resource;
import javax.jws.WebParam;
import javax.xml.ws.WebServiceContext;
import java.math.BigInteger;
import java.util.List;

/**
 * Created by Christophe Jeunesse on 08/09/2016.
 */
@ServiceDef(domain = SoapTestingServer.DOMAIN,name = "TestWebserviceSoap",version = "1.0")
public class TestWebserviceImpl extends AbstractSoapExposableService implements TestWebService {
    @Resource
    private WebServiceContext webServiceContext;

    @Override
    public TestResponse testOperation(@WebParam(partName = "parameters", name = "TestRequest", targetNamespace = "http://www.dreameddeath.com/Interfaces/test/v0/data/in") TestRequest parameters) throws TestFault {
        IGlobalContext context = (IGlobalContext)webServiceContext.getMessageContext().get(PropertyUtils.PROPERTY_GLOBAL_CONTEXT_PARAM_NAME);
        Preconditions.checkNotNull(context);
        Preconditions.checkNotNull(context.callerCtxt());
        Preconditions.checkArgument("TEST_GID1".equals(context.globalTraceId()));
        Preconditions.checkArgument("TEST_TID1".equals(context.callerCtxt().traceId()));
        TestResponse response=new TestResponse();
        response.setTid(context.currentTraceId());
        List<OffersCCOResp> offersCCORespList = response.getOffer();
        for(OffersCCO offer:parameters.getOffer()){
            OffersCCOResp offerResp = new OffersCCOResp();
            offerResp.setCode(offer.getCode());
            offerResp.setLabel(offer.getLabel());
            offerResp.setQuantity(BigInteger.valueOf(new Double(Math.random()*10).intValue()));
            offersCCORespList.add(offerResp);
        }
        return response;
    }
}
