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

package com.dreameddeath.core.process.exception;

import com.dreameddeath.core.process.model.v1.base.AbstractTask;
import com.dreameddeath.core.process.model.v1.base.ProcessState;
import com.dreameddeath.core.process.service.context.TaskContext;

/**
 * Created by Christophe Jeunesse on 27/10/2016.
 */
public class TaskObservableExecutionException extends RuntimeException {

    public TaskObservableExecutionException(TaskExecutionException e){
        super(e);
    }

    public TaskObservableExecutionException(TaskContext ctxt, String message) {
        this(new TaskExecutionException(ctxt,message));
    }

    public TaskObservableExecutionException(TaskContext ctxt, String message,Throwable e) {
        this(new TaskExecutionException(ctxt,message,e));
    }

    public TaskObservableExecutionException(AbstractTask task, ProcessState.State state, String message) {
        this(new TaskExecutionException(task,state,message));
    }

    public TaskObservableExecutionException(AbstractTask task, ProcessState.State state, String message, Throwable e) {
        this(new TaskExecutionException(task,state,message, e));
    }

    public TaskObservableExecutionException(AbstractTask task, ProcessState.State state, Throwable e) {
        this(new TaskExecutionException(task,state,e));
    }
}
