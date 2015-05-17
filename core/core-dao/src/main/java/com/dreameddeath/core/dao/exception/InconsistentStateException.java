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

package com.dreameddeath.core.dao.exception;

import com.dreameddeath.core.model.document.CouchbaseDocument;

/**
 * Created by CEAJ8230 on 21/09/2014.
 */
@SuppressWarnings("StringBufferReplaceableByString")
public class InconsistentStateException extends DaoException {
    private CouchbaseDocument _doc;
    public InconsistentStateException(CouchbaseDocument doc,Throwable e){
        super(e);
        _doc=doc;
    }
    public InconsistentStateException(CouchbaseDocument doc,String message, Throwable e){
        super(message,e);
        _doc=doc;
    }
    public InconsistentStateException(CouchbaseDocument doc,String message){
        super(message);
        _doc=doc;
    }

    public CouchbaseDocument getDocument(){
        return _doc;
    }

    @Override
    public String getMessage(){
        return new StringBuilder(super.getMessage()).append("\n The document was <").append(_doc).toString();
    }
}
