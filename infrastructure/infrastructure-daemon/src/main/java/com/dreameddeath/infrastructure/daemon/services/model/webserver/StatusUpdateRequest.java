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

package com.dreameddeath.infrastructure.daemon.services.model.webserver;

/**
 * Created by Christophe Jeunesse on 14/08/2015.
 */
public class StatusUpdateRequest {
    private Action _action;

    public Action getAction() {
        return _action;
    }

    public void setAction(Action action) {
        this._action = action;
    }

    public enum Action {
        START("start"),
        RESTART("restart"),
        STOP("stop");

        private String name;

        Action(String name){
            this.name = name;
        }

        String getName(){
            return name;
        }

        @Override
        public String toString(){
            return name;
        }
    }
}
