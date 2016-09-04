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

package com.dreameddeath.core.service.utils;

import com.dreameddeath.core.service.model.common.CuratorDiscoveryServiceDescription;
import org.apache.curator.x.discovery.ServiceInstance;

import java.util.*;

import static org.apache.curator.x.discovery.UriSpec.*;

/**
 * Created by Christophe Jeunesse on 04/09/2016.
 */
public class UriUtils {
    private static final Set<String> VARIABLE_TO_IGNORE = Collections.unmodifiableSet(new TreeSet<>(Arrays.asList(
            FIELD_SCHEME,FIELD_PORT
            ,FIELD_ADDRESS,FIELD_SSL_PORT)));

    public static String buildUri(ServiceInstance<? extends CuratorDiscoveryServiceDescription> serviceDescr){
        Map<String,Object> params = new TreeMap<>();
        serviceDescr.getUriSpec().getParts().stream()
                .filter(part -> part.isVariable() && !VARIABLE_TO_IGNORE.contains(part.getValue()))
                .forEach(part -> params.put(part.getValue(), "{" + part.getValue() + "}")
                );
        return serviceDescr.buildUriSpec(params);
    }
}
