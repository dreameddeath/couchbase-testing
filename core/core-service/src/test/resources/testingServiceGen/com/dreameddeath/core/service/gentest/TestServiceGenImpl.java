/*
 * Copyright Christophe Jeunesse
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */

package com.dreameddeath.core.service.gentest;

import com.dreameddeath.core.model.document.CouchbaseDocument;
import com.dreameddeath.core.service.annotation.*;
import com.dreameddeath.core.context.IGlobalContext;
import com.dreameddeath.core.service.swagger.TestingDocument;
import com.dreameddeath.core.service.swagger.TestingExternalElement;
import com.dreameddeath.core.user.IUser;
import org.joda.time.DateTime;
import io.reactivex.Single;
import com.dreameddeath.core.service.*;

import javax.ws.rs.HttpMethod;

/**
 * Created by Christophe Jeunesse on 05/03/2015.
 */
@ExposeService(path="/TestServiceGen",type="test",domain = "test",name = "testServiceGen",version = "1.0",status = VersionStatus.STABLE,accessType = DataAccessType.READ_WRITE)
public class TestServiceGenImpl implements ITestService {
    @Override
    @ExposeMethod(
            method=HttpMethod.POST,
            path="/toto/:rootId=input.rootId/tuto/:id=input.id",
            status = VersionStatus.STABLE
    )
    @BodyInfo(paramName = "input")
    public Single<Result> runWithRes(IGlobalContext ctxt, Input input) {
        Result res = new Result();
        res.result = "HTTP 200";
        res.id = input.id+" gen";
        res.rootId =  input.rootId+" gen";
        res.plusOneMonth = input.otherField.plusMonths(1);
        return Single.just(res);
    }


    @Override
    @ExposeMethod(
            method=HttpMethod.GET,
            path="/toto/:rootId/tuto/:id=id",
            status = VersionStatus.STABLE
    )
    public Single<Result> getWithRes(String rootId, String id) {
        Result res = new Result();
        res.result = "HTTP 200";
        res.id = id+" gen";
        res.rootId =  rootId+" gen";
        res.plusOneMonth = DateTime.now().plusMonths(1);
        return Single.just(res);
    }

    @Override
    @ExposeMethod(
            method=HttpMethod.GET,
            path="/toto/:rootId?id=id",
            status = VersionStatus.STABLE
    )
    public Single<Result> putWithQuery(String rootId, String id) {
        Result res = new Result();
        res.result = "HTTP 200";
        res.id = id+" putgen";
        res.rootId =  rootId+" putgen";
        res.plusOneMonth = DateTime.now().plusMonths(1);
        return Single.just(res);
    }

    @Override
    @ExposeMethod(
            method=HttpMethod.POST,
            path="/testingDocument",
            status = VersionStatus.STABLE
    )
    public Single<TestingDocument> initDocument(IGlobalContext ctxt) {
        TestingDocument doc = new TestingDocument();
        TestingExternalElement extElt = new TestingExternalElement();
        extElt.addDate(new DateTime());
        doc.addTestExternalEltList(extElt);
        TestingDocument.TestingInnerElement innerElement = new TestingDocument.TestingInnerElement();
        innerElement.addDate(new DateTime());
        doc.addTestCplxList(innerElement);
        return Single.just(doc);
    }


    @Override
    @ExposeMethod(
            method=HttpMethod.POST,
            path="/testingDocumentUser",
            status = VersionStatus.STABLE
    )
    public Single<TestingDocument> initDocument(IUser user) {
        TestingDocument doc = new TestingDocument();
        doc.getBaseMeta().setKey("testingdoc/1");
        doc.getBaseMeta().setCas(21);
        doc.getBaseMeta().addFlag(CouchbaseDocument.DocumentFlag.Binary);
        doc.getBaseMeta().addFlag(CouchbaseDocument.DocumentFlag.Compressed);
        doc.getBaseMeta().addFlag(CouchbaseDocument.DocumentFlag.Deleted);
        TestingExternalElement extElt = new TestingExternalElement();
        extElt.addDate(new DateTime());
        doc.addTestExternalEltList(extElt);
        TestingDocument.TestingInnerElement innerElement = new TestingDocument.TestingInnerElement();
        innerElement.addDate(new DateTime());
        doc.addTestCplxList(innerElement);
        return Single.just(doc);
    }

}
