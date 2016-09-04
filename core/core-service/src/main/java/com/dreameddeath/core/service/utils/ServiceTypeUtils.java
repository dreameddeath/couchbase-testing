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

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.ServiceLoader;

/**
 * Created by Christophe Jeunesse on 03/09/2016.
 */
public class ServiceTypeUtils {
    public static class ServiceTypeHelperLoader{
        private static final ThreadLocal<ServiceTypeHelperLoader> loaders=new ThreadLocal<ServiceTypeHelperLoader>(){
            @Override
            protected ServiceTypeHelperLoader initialValue() {
                return new ServiceTypeHelperLoader();
            }
        };

        public static final ServiceTypeHelperLoader getInstance(){
            return loaders.get();
        }

        private ServiceLoader<IServiceTypeHelper> serviceTypeHelpersLoader = ServiceLoader.load(IServiceTypeHelper.class,Thread.currentThread().getContextClassLoader());
        private Map<String,IServiceTypeHelper> helpers=new HashMap<>();

        private ServiceTypeHelperLoader(){
            helpers.clear();
            serviceTypeHelpersLoader.reload();
            Iterator<IServiceTypeHelper> helperIterator = serviceTypeHelpersLoader.iterator();
            while(helperIterator.hasNext()){
                IServiceTypeHelper helper = helperIterator.next();
                helpers.put(helper.getType(),helper);
            }
        }

        public IServiceTypeHelper getByType(String name){
            return helpers.get(name);
        }
    }


    public static IServiceTypeHelper getDefinition(String type){
        return ServiceTypeHelperLoader.getInstance().getByType(type);
    }
}
