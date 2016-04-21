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

package com.dreameddeath.compile.tools.annotation.processor.reflection;

import com.dreameddeath.compile.tools.annotation.processor.AnnotationElementType;
import com.dreameddeath.core.java.utils.ClassUtils;

import javax.lang.model.element.Element;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Christophe Jeunesse on 07/03/2015.
 */
public class PackageInfo extends AnnotatedInfo {
    private final static Map<PackageElement,PackageInfo> packageElementToInfoMap = new HashMap<>();
    private final static Map<Package,PackageInfo> packageToInfoMap = new HashMap<>();

    public static PackageInfo getPackageInfo(Package aPackage){
        synchronized (packageElementToInfoMap) {
            if (!packageToInfoMap.containsKey(aPackage)) {
                return new PackageInfo(aPackage);
            }
            return packageToInfoMap.get(aPackage);
        }
    }

    public static PackageInfo getPackageInfo(PackageElement packageElement){
        synchronized (packageElementToInfoMap) {
            if (!packageElementToInfoMap.containsKey(packageElement)) {
                return new PackageInfo(packageElement);
            }
            return packageElementToInfoMap.get(packageElement);
        }
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

    private String name;
    private PackageElement packageElement=null;
    private Package packageRef = null;
    private PackageInfo parentPackage=null;

    private void init(){
        if(packageRef!=null){
            name = packageRef.getName();
            packageToInfoMap.put(packageRef,this);
        }
        if(packageElement!=null){
            name = packageElement.getQualifiedName().toString();
            packageElementToInfoMap.put(packageElement,this);
        }
    }

    private PackageInfo(PackageElement element){
        super(element);
        packageElement=element;
        packageRef = getPackage(packageElement);
        init();
        /*parentPackage=PackageInfo.getPackageInfo(packageElement);
        if((parentPackage==this)||(parentPackage!=null && parentPackage.getName().equals(this.getName()))) {
            parentPackage=null;
        }*/
        if(parentPackage==null) {
            parentPackage=AnnotationElementType.getFirstParentTypeInfo(this);
        }
        if((parentPackage==null) && (packageRef!=null)){
            Package parent = ClassUtils.getFirstParentPackage(packageRef);
            if (parent != null) {
                parentPackage = PackageInfo.getPackageInfo(parent);
            }
        }
    }

    private PackageInfo(Package aPackage){
        super(aPackage);
        packageRef = aPackage;
        init();
        if(aPackage!=null) {
            if(parentPackage==null) {
                parentPackage=AnnotationElementType.getFirstParentTypeInfo(this);
            }
            if(parentPackage==null) {
                Package parent = ClassUtils.getFirstParentPackage(aPackage);
                if (parent != null) {
                    parentPackage = PackageInfo.getPackageInfo(parent);
                }
            }
        }
    }

    public String getName(){
        return name;
    }

    public PackageInfo getParentPackage() {
        return parentPackage;
    }
}
