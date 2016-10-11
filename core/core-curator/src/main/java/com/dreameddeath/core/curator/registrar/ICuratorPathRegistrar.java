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

package com.dreameddeath.core.curator.registrar;

import com.dreameddeath.core.curator.model.IRegistrablePathData;

import java.util.List;

/**
 * Created by Christophe Jeunesse on 05/10/2016.
 */
public interface ICuratorPathRegistrar<T extends IRegistrablePathData>{
    Result register(T obj) throws Exception;
    Result update(T obj)throws Exception;
    Result deregister(T obj)throws Exception;
    List<T> registeredList();
    void close() throws Exception;
    enum Result{
        DONE,
        IGNORED,
        UNCHANGE
    }
}
