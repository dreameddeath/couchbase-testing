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

package com.dreameddeath.core.service.registrar;

/**
 * Created by Christophe Jeunesse on 13/02/2015.
 */
public interface IRestEndPointDescription {
    String daemonUid();
    String webserverUid();
    int port();
    String path();
    String host();

    class Utils{
        public static String buildUid(IRestEndPointDescription description){
            return description.daemonUid()+"#"+description.webserverUid();
        }

        public static String getDaemonUid(String fullUid){
            String parts[] = fullUid.split("#");
            return parts[0];
        }

        public static String getServerUid(String fullUid){
            String parts[] = fullUid.split("#");
            return (parts.length>1)?parts[1]:"";
        }

    }
}
