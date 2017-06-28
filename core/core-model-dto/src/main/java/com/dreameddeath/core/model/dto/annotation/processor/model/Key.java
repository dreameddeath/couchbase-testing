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

package com.dreameddeath.core.model.dto.annotation.processor.model;

import com.dreameddeath.core.model.dto.annotation.DtoInOutMode;

/**
 * Created by christophe jeunesse on 13/06/2017.
 */
public class Key {
    private final String type;
    private final String packageName;
    private final String className;
    private final String version;
    private final DtoInOutMode inOutMode;

    public Key(String packageName, String className, DtoInOutMode inOutMode, String type, String version) {
        this.version = version;
        this.type = type;
        this.packageName = packageName;
        this.className = className;
        this.inOutMode = inOutMode;
    }

    public String getVersion() {
        return version;
    }

    public String getType() {
        return type;
    }

    public String getPackageName() {
        return packageName;
    }

    public String getClassName() {
        return className;
    }

    public DtoInOutMode getInOutMode() {
        return inOutMode;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Key key = (Key) o;

        if (!version.equals(key.version)) return false;
        if (type != null ? !type.equals(key.type) : key.type != null) return false;
        if (!packageName.equals(key.packageName)) return false;
        if (!className.equals(key.className)) return false;
        return inOutMode == key.inOutMode;
    }

    @Override
    public int hashCode() {
        int result = version.hashCode();
        result = 31 * result + (type != null ? type.hashCode() : 0);
        result = 31 * result + packageName.hashCode();
        result = 31 * result + className.hashCode();
        result = 31 * result + inOutMode.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "Key{" +
                "type='" + type + '\'' +
                ", packageName='" + packageName + '\'' +
                ", className='" + className + '\'' +
                ", version='" + version + '\'' +
                ", inOutMode=" + inOutMode +
                '}';
    }
}
