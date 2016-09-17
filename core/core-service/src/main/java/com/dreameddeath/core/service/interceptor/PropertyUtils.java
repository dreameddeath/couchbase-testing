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

package com.dreameddeath.core.service.interceptor;

import com.dreameddeath.core.context.IGlobalContext;
import com.dreameddeath.core.log.MDCUtils;
import com.dreameddeath.core.user.IUser;
import org.joda.time.DateTime;

/**
 * Created by Christophe Jeunesse on 13/09/2016.
 */
public interface PropertyUtils {
    String PROPERTY_USER_PARAM_NAME = IUser.class.getName();
    String PROPERTY_USER_TOKEN_PARAM_NAME = IUser.class.getName()+"#Token";
    String PROPERTY_GLOBAL_CONTEXT_PARAM_NAME = IGlobalContext.class.getName();
    String PROPERTY_START_TIME_NANO_PARAM_NAME = DateTime.class+"#start";
    String PROPERTY_MDC_CONTEXT = MDCUtils.class+"#contextMap";

}
