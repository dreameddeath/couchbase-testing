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

package com.dreameddeath.core.log;

import org.slf4j.MDC;
import rx.functions.Action0;
import rx.functions.Func1;

import java.util.Map;

/**
 * Created by Christophe Jeunesse on 18/09/2016.
 */
public class MdcSchedulerHook implements Func1<Action0,Action0> {
    @Override
    public Action0 call(Action0 action0) {
        return new MdcAction0Wrapper(action0);
    }

    static class MdcAction0Wrapper implements Action0 {
        private final Action0 actual;
        private final Map<String, String> mdcContextMap;

        public MdcAction0Wrapper(Action0 actual) {
            this.actual = actual;
            mdcContextMap = MDC.getCopyOfContextMap();
        }

        @Override
        public void call() {
            final Map<String,String> oldMdcContextMap=MDC.getCopyOfContextMap();

            try {
                if(mdcContextMap!=null) {
                    MDC.setContextMap(mdcContextMap);
                }
                else{
                    MDC.clear();
                }
                actual.call();
            } finally {
                if(oldMdcContextMap!=null) {
                    MDC.setContextMap(oldMdcContextMap);
                }
                else{
                    MDC.clear();
                }
            }
        }
    }
}