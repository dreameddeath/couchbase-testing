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

package com.dreameddeath.core.event;

import com.dreameddeath.core.process.common.AbstractJob;
import com.dreameddeath.core.process.common.AbstractTask;

/**
 * Created by Christophe Jeunesse on 27/07/2014.
 */
public class TaskProcessEvent {
    private AbstractTask _task;

    public TaskProcessEvent(AbstractTask task){
        _task = task;
    }

    public AbstractTask getTask(){
        return _task;
    }

    public AbstractJob getJob(){
        return _task.getParentJob();
    }

}
