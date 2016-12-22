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


package com.dreameddeath.core.couchbase.dcp;

import com.couchbase.client.core.message.CouchbaseMessage;
import com.couchbase.client.core.message.dcp.MutationMessage;
import com.couchbase.client.core.message.dcp.RemoveMessage;
import com.couchbase.client.core.message.dcp.SnapshotMarkerMessage;

/**
 * Created by Christophe Jeunesse on 27/05/2015.
 */
public class DCPEvent {
    private CouchbaseMessage message;
    private Type type=Type.UNKNOWN;


    public DCPEvent setMessage(final CouchbaseMessage message) {
        this.message = message;
        if(message instanceof MutationMessage){
            type = Type.MUTATION;
        }
        else if(message instanceof RemoveMessage){
            type = Type.DELETION;
        }
        else if(message instanceof SnapshotMarkerMessage){
            type = Type.SNAPSHOT;
        }
        return this;
    }

    public Type getType(){
        return type;
    }

    public CouchbaseMessage message() {
        return message;
    }

    @SuppressWarnings("unchecked")
    public MutationMessage asMutationMessage(){
        return (MutationMessage) message;
    }

    @SuppressWarnings("unchecked")
    public SnapshotMarkerMessage asSnapshotMessage(){
        return (SnapshotMarkerMessage) message;
    }

    @SuppressWarnings("unchecked")
    public RemoveMessage asDeletionMessage(){
        return (RemoveMessage) message;
    }


    public enum Type{
        SNAPSHOT,
        MUTATION,
        DELETION,
        UNKNOWN
    }
}
