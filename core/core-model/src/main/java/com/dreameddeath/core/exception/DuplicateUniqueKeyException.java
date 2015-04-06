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

package com.dreameddeath.core.exception;

import com.dreameddeath.core.model.document.CouchbaseDocument;
import com.dreameddeath.core.model.unique.CouchbaseUniqueKey;

/**
 * Created by ceaj8230 on 20/11/2014.
 */
public class DuplicateUniqueKeyException extends Exception {
    CouchbaseDocument _doc;
    String _key;
    CouchbaseUniqueKey _uniqueKeyDoc;
    String _ownerDocumentKey;

    public DuplicateUniqueKeyException(String key,String ownerDocumentKey,CouchbaseDocument requestingDoc,CouchbaseUniqueKey uniqueKeyDoc,String message) {
        super(message);
        _doc = requestingDoc;
        _key = key;
        _ownerDocumentKey = ownerDocumentKey;
        _uniqueKeyDoc = uniqueKeyDoc;
    }

    public DuplicateUniqueKeyException(String key,String ownerDocumentKey,CouchbaseDocument requestingDoc,CouchbaseUniqueKey uniqueKeyDoc) {
        this(key,ownerDocumentKey,requestingDoc,uniqueKeyDoc,
             "The key <"+key+"> requested by doc<"+ requestingDoc.getBaseMeta().getKey()+">is already used by the document <"+ownerDocumentKey+">");
    }


    @Override
    public String getMessage(){
        return super.getMessage() + "\n The doc was " + _doc;
    }
}
