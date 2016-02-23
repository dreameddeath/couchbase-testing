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

package com.dreameddeath.core.dao.exception;

import com.dreameddeath.core.model.document.CouchbaseDocument;
import com.dreameddeath.core.model.exception.DuplicateUniqueKeyException;
import com.dreameddeath.core.model.unique.CouchbaseUniqueKey;

/**
 * Created by Christophe Jeunesse on 05/08/2014.
 */
public class DuplicateUniqueKeyStorageException extends DocumentStorageException {
    public DuplicateUniqueKeyStorageException(CouchbaseDocument doc, String message, DuplicateUniqueKeyException e){ super(doc,message,e);}
    public DuplicateUniqueKeyStorageException(CouchbaseDocument doc, DuplicateUniqueKeyException e){ super(doc,e);}

    @Override
    public DuplicateUniqueKeyException getCause(){
        return (DuplicateUniqueKeyException) super.getCause();
    }

    public CouchbaseDocument getDoc() {
        return (getCause()!=null)?getCause().getDoc():null;
    }

    public String getKey() {
        return (getCause()!=null)?getCause().getKey():null;
    }

    public CouchbaseUniqueKey getUniqueKeyDoc() {
        return (getCause()!=null)?getCause().getUniqueKeyDoc():null;
    }

    public String getOwnerDocumentKey() {
        return (getCause()!=null)?getCause().getOwnerDocumentKey():null;
    }
}
