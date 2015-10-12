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

package com.dreameddeath.core.model.entity;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Christophe Jeunesse on 12/10/2015.
 */
public class EntityVersion implements Comparable<EntityVersion>{
    public static final EntityVersion DEFAULT_VERSION = EntityVersion.version(1,0,0);
    public static final String VERSION_PATTERN_STR = "[Vv]?(\\d+)\\.(\\d+)(?:\\.(\\d+))?";
    public static final Pattern VERSION_PATTERN = Pattern.compile("^"+VERSION_PATTERN_STR+"$");
    private Integer _major=0;
    private Integer _minor=0;
    private Integer _patch=null;

    public EntityVersion(String version){
        parseString(version);
    }

    public EntityVersion(Integer major, Integer minor, Integer patch){
        //TODO assert not null and positive
        _major = major;
        _minor = minor;
        _patch = patch;
    }

    private void parseString(String versionStr){
        Matcher matcher = VERSION_PATTERN.matcher(versionStr);
        if(matcher.matches()){
            _major = Integer.parseInt(matcher.group(1));
            _minor = Integer.parseInt(matcher.group(2));
            _patch = (matcher.group(3)!=null)?Integer.parseInt(matcher.group(3)):null;
        }
    }

    public Integer getMajor() {
        return _major;
    }

    public Integer getMinor() {
        return _minor;
    }

    public Integer getPatch() {
        return _patch;
    }

    @Override
    public String toString(){
        if(_patch==null) {
            return String.format("%d.%d.0", _major, _minor);
        }
        else {
            return String.format("%d.%d.%d", _major, _minor, _patch);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        EntityVersion entityVersion = (EntityVersion) o;

        if (!_major.equals(entityVersion._major)) return false;
        if (!_minor.equals(entityVersion._minor)) return false;
        return !(_patch != null ? !_patch.equals(entityVersion._patch) : entityVersion._patch != null);
    }

    @Override
    public int hashCode() {
        int result = _major.hashCode();
        result = 31 * result + _minor.hashCode();
        result = 31 * result + (_patch != null ? _patch.hashCode() : 0);
        return result;
    }

    @Override
    public int compareTo(EntityVersion o) {
        int result = _major.compareTo(o._major);
        if(result!=0) return result;
        result=_minor.compareTo(o._minor);
        if(result!=0) return result;
        //Null means match everything
        if((_patch==null)||(o._patch==null)) return 0;
        return _patch.compareTo(o._patch);
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
