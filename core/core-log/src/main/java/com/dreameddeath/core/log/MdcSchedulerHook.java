/*
 * Copyright Christophe Jeunesse
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */

package com.dreameddeath.core.log;

import io.reactivex.functions.Function;
import org.slf4j.MDC;

import java.util.Map;

/**
 * Created by Christophe Jeunesse on 18/09/2016.
 */
public class MdcSchedulerHook implements Function<Runnable,Runnable> {
    @Override
    public Runnable apply(Runnable action0) {
        return new MdcActionWrapper(action0);
    }

    static class MdcActionWrapper implements Runnable {
        private final Runnable actual;
        private final Map<String, String> mdcContextMap;

        public MdcActionWrapper(Runnable actual) {
            this.actual = actual;
            mdcContextMap = MDC.getCopyOfContextMap();
        }

        @Override
        public void run(){
            final Map<String,String> oldMdcContextMap=MDC.getCopyOfContextMap();

            try {
                if(mdcContextMap!=null) {
                    MDC.setContextMap(mdcContextMap);
                }
                else{
                    MDC.clear();
                }
                actual.run();
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