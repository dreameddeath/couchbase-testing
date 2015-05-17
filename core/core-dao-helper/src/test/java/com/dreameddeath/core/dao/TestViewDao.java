/*
 * Copyright Christophe Jeunesse
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package com.dreameddeath.core.dao;

import com.dreameddeath.core.dao.model.view.IViewKeyTranscoder;
import com.dreameddeath.core.dao.model.view.IViewTranscoder;
import com.dreameddeath.core.dao.view.CouchbaseViewDao;

/**
 * Created by CEAJ8230 on 15/04/2015.
 */
public class TestViewDao extends CouchbaseViewDao<String, String, TestDoc> {
    public TestViewDao(TestDao parentDao) {
        super("test/", "testView", parentDao);
    }

    @Override
    public String getContent() {
        return
                "emit(meta.id,doc);\n" +
                "emit(doc.strVal,doc.strVal);\n" +
                "emit(doc.doubleVal,doc.doubleVal);\n" +
                "emit(doc.intVal,doc.intVal);\n" +
                "emit(doc.boolVal,doc.boolVal);\n" +
                "emit(doc.longVal,doc.longVal);\n" +
                "emit(doc.arrayVal,doc.arrayVal);\n";
    }

    @Override
    public IViewTranscoder<String> getValueTranscoder() {
        return IViewTranscoder.Utils.stringTranscoder();
    }

    @Override
    public IViewKeyTranscoder<String> getKeyTranscoder() {
        return IViewTranscoder.Utils.stringKeyTranscoder();
    }
}
