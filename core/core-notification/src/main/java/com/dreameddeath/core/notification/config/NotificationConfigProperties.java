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

package com.dreameddeath.core.notification.config;

import com.dreameddeath.core.config.ConfigPropertyFactory;
import com.dreameddeath.core.config.annotation.ConfigPropertyDoc;
import com.dreameddeath.core.config.annotation.ConfigPropertyPackage;
import com.dreameddeath.core.config.impl.IntConfigProperty;

/**
 * Created by Christophe Jeunesse on 10/10/2015.
 */
@ConfigPropertyPackage(name="notification",domain = "core",descr = "All common properties for notification classes")
public class NotificationConfigProperties {
    @ConfigPropertyDoc(
            name="core.notification.eventbus.buffer_size",
            descr = "defines the buffer size for ring buffer. Will be aligned to nearest the power of two just below or equals",
            examples = {"64"}
    )
    public static final IntConfigProperty EVENTBUS_BUFFER_SIZE = ConfigPropertyFactory.getIntProperty("core.notification.eventbus.buffer_size",50);

    @ConfigPropertyDoc(
            name="core.notification.eventbus.thread_pool_size",
            descr = "defines the thread pool size for ring buffer",
            examples = {"50"}
    )
    public static final IntConfigProperty EVENTBUS_THREAD_POOL_SIZE = ConfigPropertyFactory.getIntProperty("core.notification.eventbus.thread_pool_size",Runtime.getRuntime().availableProcessors());
}
