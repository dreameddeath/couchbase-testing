/*
 * 	Copyright Christophe Jeunesse
 *
 * 	Licensed under the Apache License, Version 2.0 (the "License");
 * 	you may not use this file except in compliance with the License.
 * 	You may obtain a copy of the License at
 *
 * 	http://www.apache.org/licenses/LICENSE-2.0
 *
 * 	Unless required by applicable law or agreed to in writing, software
 * 	distributed under the License is distributed on an "AS IS" BASIS,
 * 	WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * 	See the License for the specific language governing permissions and
 * 	limitations under the License.
 *
 */

package com.dreameddeath.core.json.model;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonValue;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Christophe Jeunesse on 12/10/2015.
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonIgnoreProperties(ignoreUnknown=true)
@JsonAutoDetect(getterVisibility= JsonAutoDetect.Visibility.NONE,fieldVisibility= JsonAutoDetect.Visibility.NONE,isGetterVisibility = JsonAutoDetect.Visibility.NONE,setterVisibility = JsonAutoDetect.Visibility.NONE,creatorVisibility = JsonAutoDetect.Visibility.NONE)
public final class Version implements Comparable<Version>{
    public static final Version DEFAULT_VERSION = Version.version(1,0,0);
    public static final String DEFAULT_VERSION_STR = "1.0.0";
    public static final String VERSION_PATTERN_STR = "[Vv]?(\\d+)\\.(\\d+)(?:\\.(\\d+))?";
    public static final Pattern VERSION_PATTERN = Pattern.compile("^"+VERSION_PATTERN_STR+"$");
    public static final Version EMPTY_VERSION = new Version(null,null,null);

    private Integer major=0;
    private Integer minor=0;
    private Integer patch=null;

    @JsonCreator
    public Version(String version){
        parseString(version);
    }

    public Version(Integer major, Integer minor, Integer patch){
        //TODO assert not null for major and positive
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

    @Override @JsonValue
    public String toString(){
        return String.format("%d.%d.%d", major, minor, patch!=null?patch:0);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Version Version = (Version) o;

        if (!major.equals(Version.major)) return false;
        if (!minor.equals(Version.minor)) return false;
        return (patch==null)||(Version.patch==null) || patch.equals(Version.patch);
    }

    @Override
    public int hashCode() {
        int result = major.hashCode();
        result = 31 * result + minor.hashCode();
        //result = 31 * result + (patch != null ? patch.hashCode() : 0);
        return result;
    }

    @Override
    public int compareTo(Version o) {
        int result = major.compareTo(o.major);
        if(result!=0) return result;
        result=minor.compareTo(o.minor);
        if(result!=0) return result;
        //Null means match everything
        if((patch==null)||(o.patch==null)) return 0;
        return patch.compareTo(o.patch);
    }

    public static Version version(String major,String minor,String patch){
        return new Version(Integer.parseInt(major),Integer.parseInt(minor),patch!=null?Integer.parseInt(patch):null);
    }

    public static Version version(Integer major,Integer minor,Integer patch){
        return new Version(major,minor,patch);
    }

    public static Version version(String version){
        return new Version(version);
    }
}
