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

package com.dreameddeath.couchbase.core.process.remote.model.rest;

import com.dreameddeath.core.process.model.ProcessState;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Created by Christophe Jeunesse on 04/01/2016.
 */
public class StateInfo {
    @JsonProperty
    public State state;
    @JsonProperty
    public String lastRunError;

    public StateInfo(){}
    public StateInfo(ProcessState sourceState){
        state = State.mapFrom(sourceState.getState());
        lastRunError = sourceState.getLastRunError();
    }


    public enum State{
        unknown(ProcessState.State.UNKNOWN),
        created(ProcessState.State.NEW),
        asyncCreated(ProcessState.State.ASYNC_NEW),
        initialized(ProcessState.State.INITIALIZED), //Init done
        preprocessed(ProcessState.State.PREPROCESSED),
        processed(ProcessState.State.PROCESSED), //Processing Done
        postprocessed(ProcessState.State.POSTPROCESSED), //Job Update Processing done
        done(ProcessState.State.DONE);//Cleaning done

        private final ProcessState.State internal;

        State(ProcessState.State internal){
            this.internal = internal;
        }


        public ProcessState.State toProcessSate(){
            return internal;
        }

        public static State mapFrom(ProcessState.State source){
            for(State currState:State.values()){
                if(currState.toProcessSate()==source){
                    return currState;
                }
            }
            return State.unknown;
        }
    }

}
