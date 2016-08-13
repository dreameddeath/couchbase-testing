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

package com.dreameddeath.core.notification.listener.impl;

import com.dreameddeath.core.notification.model.v1.listener.ListenerDescription;

import java.util.Map;

/**
 * Created by Christophe Jeunesse on 12/08/2016.
 */
public interface IEventListenerTypeMatcher {
    boolean isMatching(String type,Map<String,String> params);
    boolean isMatching(ListenerDescription description);
    int getMatchingRank(String type,Map<String,String> params);
    int getMatchingRank(ListenerDescription description);
}
