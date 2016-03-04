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

package com.tests;

import com.dreameddeath.core.model.annotation.DocumentDef;
import com.dreameddeath.core.model.annotation.DocumentProperty;
import com.dreameddeath.core.process.model.AbstractJob;
import com.dreameddeath.core.process.model.DocumentUpdateTask;
import com.dreameddeath.core.validation.annotation.NotNull;
import com.dreameddeath.couchbase.core.process.remote.annotation.Request;
import com.dreameddeath.couchbase.core.process.remote.annotation.RestExpose;
import com.dreameddeath.couchbase.core.process.remote.annotation.Result;

/**
 * Created by Christophe Jeunesse on 04/03/2016.
 */
@DocumentDef(domain="test")
@RestExpose(rootPath = "testdocjobs/genupdate",domain = "tests",name = "testdocjobupdategen")
public class TestGeneration extends AbstractJob {
    @DocumentProperty @NotNull @Request
    public Integer decrIntValue;
    @DocumentProperty @NotNull @Request
    private String docKey;

    @DocumentProperty @Result
    public Integer resultIncrValue;
    @DocumentProperty @Result
    private String outputString;


    public String getDocKey(){
        return docKey;
    }

    public void setDocKey(String key){
        this.docKey=key;
    }

    public String getOutputString() {
        return outputString;
    }

    public void setOutputString(String outputString) {
        this.outputString = outputString;
    }
}
