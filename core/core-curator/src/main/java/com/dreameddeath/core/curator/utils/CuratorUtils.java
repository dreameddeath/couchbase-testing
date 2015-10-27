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

package com.dreameddeath.core.curator.utils;

import com.dreameddeath.core.curator.model.IRegisterable;
import org.apache.curator.framework.CuratorFramework;

/**
 * Created by Christophe Jeunesse on 26/10/2015.
 */
public class CuratorUtils {
    public static void createPathIfNeeded(CuratorFramework curatorFramework, String path){
        if (!path.startsWith("/")) {
            path = "/" + path;
        }
        //boolean exists = false;
        try {
            if((curatorFramework.checkExists().forPath(path))!=null){
                return;
            }
        }
        catch(Exception e){
            throw new RuntimeException("Cannot stat path <"+path+">",e);
        }
        try{
            curatorFramework.create().creatingParentsIfNeeded().forPath(path);
            return; //Successfull creation
        }
        catch(Exception e){
            //Ignore
        }

        try {
            if((curatorFramework.checkExists().forPath(path))!=null){
                return;
            }
        }
        catch(Exception e){
            throw new RuntimeException("Cannot stat path <"+path+"> for creation",e);
        }

        throw new RuntimeException("Cannot create path <"+path+">");
    }

    public static String buildPath(String basePath,IRegisterable obj){
        if (!basePath.startsWith("/")) {
            basePath = "/" + basePath;
        }
        return basePath + "/" + obj.getUid();
    }


}
