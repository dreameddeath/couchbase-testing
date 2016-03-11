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

package com.dreameddeath.core.process.model.tasks;

import com.dreameddeath.core.model.annotation.DocumentProperty;
import com.dreameddeath.core.model.property.ListProperty;
import com.dreameddeath.core.model.property.impl.ArrayListProperty;
import com.dreameddeath.core.process.model.base.AbstractTask;

import java.util.Collection;
import java.util.List;

/**
 * Created by Christophe Jeunesse on 04/02/2016.
 */
public class SubTaskCreatorTask extends AbstractTask {
    /**
     *  subTasks : List of sub tasks created
     */
    @DocumentProperty("subTasks")
    private ListProperty<String> subTasks = new ArrayListProperty<>(SubTaskCreatorTask.this);

    /**
     * Getter of subTasks
     * @return the content
     */
    public List<String> getSubTasks() { return subTasks.get(); }
    /**
     * Setter of subTasks
     * @param vals the new collection of values
     */
    public void setSubTasks(Collection<String> vals) { subTasks.set(vals); }
    /**
     * Add a new entry to the property subTasks
     * @param val the new entry to be added
     */
    public boolean addSubTasks(String val){ return subTasks.add(val); }
    /**
     * Remove an entry to the property subTasks
     * @param val the entry to be remove
     * @return true if the entry has been removed
     */
    public boolean removeSubTasks(String val){ return subTasks.remove(val); }
}
