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

package com.dreameddeath.core.tools.annotation.processor.reflection;

import javax.lang.model.element.Element;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Christophe Jeunesse on 07/03/2015.
 */
public class PackageInfo extends AnnotatedInfo {
    private static Map<PackageElement,PackageInfo> _packageElementToInfoMap = new HashMap<>();
    private static Map<Package,PackageInfo> _packageToInfoMap = new HashMap<>();


    public static PackageInfo getPackageInfo(Package aPackage){
        if(!_packageToInfoMap.containsKey(aPackage)){
            return new PackageInfo(aPackage);
        }
        return _packageToInfoMap.get(aPackage);
    }

    public static PackageInfo getPackageInfo(PackageElement packageElement){
        if(!_packageElementToInfoMap.containsKey(packageElement)) {
            return new PackageInfo(packageElement);
        }

        return _packageElementToInfoMap.get(packageElement);
    }

    public static PackageInfo getPackageInfo(TypeElement element){
        PackageElement packageElement = null;
        Element parentElement = element.getEnclosingElement();
        while(parentElement!=null){
            if(parentElement instanceof PackageElement){
                packageElement = (PackageElement)parentElement;
                break;
            }
            parentElement = parentElement.getEnclosingElement();
        }
        return getPackageInfo(packageElement);
    }

    private static Package getPackage(PackageElement elt){
        return Package.getPackage(elt.getQualifiedName().toString());
    }

    private String _name;
    private PackageElement _packageElement=null;
    private Package _package = null;

    private void init(){
        if(_package!=null){
            _name = _package.getName();
            _packageToInfoMap.put(_package,this);
        }
        if(_packageElement!=null){
            _name = _packageElement.getQualifiedName().toString();
            _packageElementToInfoMap.put(_packageElement,this);
        }
    }

    private PackageInfo(PackageElement element){
        super(element);
        _packageElement=element;
        _package = getPackage(_packageElement);
        init();
    }

    private PackageInfo(Package aPackage){
        super(aPackage);
        _package = aPackage;
        init();
    }

    public String getName(){
        return _name;
    }
}
