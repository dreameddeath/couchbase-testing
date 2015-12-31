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

package com.dreameddeath.core.process.exception;


import com.dreameddeath.core.process.model.AbstractTask;
import com.dreameddeath.core.process.model.ProcessState.State;
import com.dreameddeath.core.process.service.context.TaskContext;

/**
 * Created by Christophe Jeunesse on 05/08/2014.
 */
public class TaskExecutionException extends Exception {
    State state;
    AbstractTask task;

    public TaskExecutionException(TaskContext ctxt, String message) {
        super(message);
        this.task = ctxt.getTask();
        this.state = ctxt.getTaskState().getState();
    }

    public TaskExecutionException(TaskContext ctxt, String message,Throwable e) {
        super(message,e);
        this.task = ctxt.getTask();
        this.state = ctxt.getTaskState().getState();
    }

    public TaskExecutionException(AbstractTask task, State state, String message) {
        super(message);
        this.task = task;
        this.state = state;
    }

    public TaskExecutionException(AbstractTask task, State state, String message, Throwable e) {
        super(message, e);
        this.task = task;
        this.state = state;
    }

    public TaskExecutionException(AbstractTask task, State state, Throwable e) {
        super(e);
        this.task = task;
        this.state = state;
    }

    public AbstractTask getTask(){ return task;}
    public State getState(){ return state;}


    @Override
    public String toString(){
        StringBuffer sb = new StringBuffer();
        sb.append("Task[").append(task.getClass()).append("/").append(task.getJobUid()).append("/").append(task.getId()).append("] ");
        sb.append(super.toString());
        return sb.toString();
    }
}
