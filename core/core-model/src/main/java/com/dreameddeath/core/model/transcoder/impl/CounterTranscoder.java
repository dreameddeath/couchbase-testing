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

package com.dreameddeath.core.model.transcoder.impl;

import com.dreameddeath.core.model.counter.CouchbaseCounter;
import com.dreameddeath.core.model.exception.transcoder.DocumentDecodingException;
import com.dreameddeath.core.model.exception.transcoder.DocumentEncodingException;
import com.dreameddeath.core.model.transcoder.ITranscoder;

/**
 * Created by Christophe Jeunesse on 12/06/2015.
 */
public class CounterTranscoder implements ITranscoder<CouchbaseCounter> {

    @Override
    public Class<CouchbaseCounter> getBaseClass() {
        return CouchbaseCounter.class;
    }

    @Override
    public CouchbaseCounter decode(byte[] buf) throws DocumentDecodingException {
        try{
            Long result = Long.parseLong(new String(buf));
            return new CouchbaseCounter(result);
        }
        catch(NumberFormatException e){
            throw new DocumentDecodingException("Cannot decode long value",buf,e);
        }
    }

    @Override
    public byte[] encode(CouchbaseCounter doc) throws DocumentEncodingException {
        return Long.toString(doc.getCurrent()).getBytes();
    }
}
