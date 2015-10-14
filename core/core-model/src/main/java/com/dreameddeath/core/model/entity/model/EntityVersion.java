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

package com.dreameddeath.core.model.entity.model;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Christophe Jeunesse on 12/10/2015.
 */
public class EntityVersion implements Comparable<EntityVersion>{
    public static final EntityVersion DEFAULT_VERSION = EntityVersion.version(1,0,0);
    public static final String VERSION_PATTERN_STR = "[Vv]?(\\d+)\\.(\\d+)(?:\\.(\\d+))?";
    public static final Pattern VERSION_PATTERN = Pattern.compile("^"+VERSION_PATTERN_STR+"$");
    private Integer major=0;
    private Integer minor=0;
    private Integer patch=null;

    public EntityVersion(String version){
        parseString(version);
    }

    public EntityVersion(Integer major, Integer minor, Integer patch){
        //TODO assert not null and positive
        this.major = major;
        this.minor = minor;
        this.patch = patch;
    }

    private void parseString(String versionStr){
        Matcher matcher = VERSION_PATTERN.matcher(versionStr);
        if(matcher.matches()){
            major = Integer.parseInt(matcher.group(1));
            minor = Integer.parseInt(matcher.group(2));
            patch = (matcher.group(3)!=null)?Integer.parseInt(matcher.group(3)):null;
        }
    }

    public Integer getMajor() {
        return major;
    }

    public Integer getMinor() {
        return minor;
    }

    public Integer getPatch() {
        return patch;
    }

    @Override
    public String toString(){
        if(patch==null) {
            return String.format("%d.%d.0", major, minor);
        }
        else {
            return String.format("%d.%d.%d", major, minor, patch);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        EntityVersion entityVersion = (EntityVersion) o;

        if (!major.equals(entityVersion.major)) return false;
        if (!minor.equals(entityVersion.minor)) return false;
        return !(patch != null ? !patch.equals(entityVersion.patch) : entityVersion.patch != null);
    }

    @Override
    public int hashCode() {
        int result = major.hashCode();
        result = 31 * result + minor.hashCode();
        result = 31 * result + (patch != null ? patch.hashCode() : 0);
        return result;
    }

    @Override
    public int compareTo(EntityVersion o) {
        int result = major.compareTo(o.major);
        if(result!=0) return result;
        result=minor.compareTo(o.minor);
        if(result!=0) return result;
        //Null means match everything
        if((patch==null)||(o.patch==null)) return 0;
        return patch.compareTo(o.patch);
    }

    public static EntityVersion version(String major,String minor,String patch){
        return new EntityVersion(Integer.parseInt(major),Integer.parseInt(minor),patch!=null?Integer.parseInt(patch):null);
    }

    public static EntityVersion version(Integer major,Integer minor,Integer patch){
        return new EntityVersion(major,minor,patch);
    }

    public static EntityVersion version(String version){
        return new EntityVersion(version);
    }
}
