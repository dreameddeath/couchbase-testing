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

package com.dreameddeath.core.process.model.v1.base;

import com.dreameddeath.core.model.annotation.DocumentProperty;
import com.dreameddeath.core.model.document.CouchbaseDocumentElement;
import com.dreameddeath.core.model.property.Property;
import com.dreameddeath.core.model.property.impl.StandardProperty;
import com.dreameddeath.core.validation.annotation.NotNull;

/**
 * Created by Christophe Jeunesse on 29/12/2015.
 */
public class ProcessState extends CouchbaseDocumentElement {
    @DocumentProperty(value = "state") @NotNull
    private Property<State> state=new StandardProperty<>(ProcessState.this, State.NEW);
    @DocumentProperty("lastRunError")
    private Property<String> errorName=new StandardProperty<String>(ProcessState.this);

    // state accessors
    public State getState() { return state.get(); }
    public void setState(State state) { this.state.set(state); }
    // error
    public String getLastRunError(){return errorName.get();}
    public void setLastRunError(String errorName){this.errorName.set(errorName);}

    public boolean isInitialized(){ return state.get().compareTo(State.INITIALIZED)>=0; }
    public boolean isPrepared(){ return state.get().compareTo(State.PREPROCESSED)>=0; }
    public boolean isProcessed(){ return state.get().compareTo(State.PROCESSED)>=0; }
    public boolean isFinalized(){ return state.get().compareTo(State.POSTPROCESSED)>=0; }
    public boolean isJobUpdated(){ return state.get().compareTo(State.JOBUPDATED)>=0; }
    public boolean isDone(){ return state.get().compareTo(State.DONE)>=0; }

    public enum State{
        UNKNOWN,
        NEW,
        ASYNC_NEW,
        INITIALIZED, //Init done
        PREPROCESSED,
        PROCESSED, //Processing Done
        POSTPROCESSED,
        JOBUPDATED, //Task only
        DONE//Cleaning done
    }

}
