/*
 * 	Copyright Christophe Jeunesse
 *
 * 	Licensed under the Apache License, Version 2.0 (the "License");
 * 	you may not use this file except in compliance with the License.
 * 	You may obtain a copy of the License at
 *
 * 	http://www.apache.org/licenses/LICENSE-2.0
 *
 * 	Unless required by applicable law or agreed to in writing, software
 * 	distributed under the License is distributed on an "AS IS" BASIS,
 * 	WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * 	See the License for the specific language governing permissions and
 * 	limitations under the License.
 *
 */

package com.dreameddeath.core.process.exception;


import com.dreameddeath.core.notification.bus.EventFireResult;
import com.dreameddeath.core.process.model.v1.base.AbstractTask;
import com.dreameddeath.core.process.model.v1.base.ProcessState.State;
import com.dreameddeath.core.process.service.context.TaskContext;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by Christophe Jeunesse on 05/08/2014.
 */
public class TaskExecutionException extends Exception {
    private final State state;
    private final AbstractTask task;
    private final List<EventFireResult<?,?>> failedNotifications=new ArrayList<>();

    public TaskExecutionException(TaskContext ctxt, String message) {
        super(message);
        this.task = ctxt.getInternalTask();
        this.state = ctxt.getTaskState().getState();
    }

    public TaskExecutionException(TaskContext ctxt, String message,Throwable e) {
        super(message,e);
        this.task = ctxt.getInternalTask();
        this.state = ctxt.getTaskState().getState();
    }

    public TaskExecutionException(TaskContext ctxt, String message,List<EventFireResult<?,?>> listFailedNotifications) {
        super(message);
        this.task = ctxt.getInternalTask();
        this.state = ctxt.getTaskState().getState();
        this.failedNotifications.addAll(listFailedNotifications);
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

    public List<EventFireResult<?,?>> getFailedNotifications() {
        return Collections.unmodifiableList(failedNotifications);
    }

    @Override
    public String toString(){
        StringBuilder sb = new StringBuilder();
        sb.append("Task[").append(task.getClass()).append("/").append(task.getJobUid()).append("/").append(task.getId()).append("] ");
        sb.append(super.toString());
        return sb.toString();
    }
}
