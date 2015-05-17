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

package com.dreameddeath.core.process.exception;


import com.dreameddeath.core.process.model.AbstractTask;

/**
 * Created by Christophe Jeunesse on 05/08/2014.
 */
public class TaskExecutionException extends Exception {
    AbstractTask.State _state;
    AbstractTask _task;

    public TaskExecutionException(AbstractTask task, AbstractTask.State state, String message) {
        super(message);
        _task = task;
        _state = state;
    }

    public TaskExecutionException(AbstractTask task, AbstractTask.State state, String message, Throwable e) {
        super(message, e);
        _task = task;
        _state = state;
    }

    public TaskExecutionException(AbstractTask task, AbstractTask.State state, Throwable e) {
        super(e);
        _task = task;
        _state = state;
    }

    public AbstractTask getTask(){ return _task;}
    public AbstractTask.State getState(){ return _state;}

}
