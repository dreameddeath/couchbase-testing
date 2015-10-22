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

import javax.lang.model.element.Element;
import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Member;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Christophe Jeunesse on 16/03/2015.
 */
public abstract class MemberInfo extends AnnotatedInfo {
    private AbstractClassInfo parent;
    private Member member;
    private Element element;
    private List<ModifierInfo> modifierInfos=new ArrayList<>();

    private void init(){
        if(element!=null){
            for(javax.lang.model.element.Modifier modifier: element.getModifiers()){
                ModifierInfo modifierInfo = ModifierInfo.valueOf(modifier);
                if(modifierInfo!=null) {
                    modifierInfos.add(modifierInfo);
                }
            }
        }
        else{
            int modifier = member.getModifiers();
            if(Modifier.isPrivate(modifier)){
                modifierInfos.add(ModifierInfo.PRIVATE);
            }
            else if(Modifier.isPublic(modifier)){
                modifierInfos.add(ModifierInfo.PUBLIC);
            }
            else if(Modifier.isProtected(modifier)){
                modifierInfos.add(ModifierInfo.PROTECTED);
            }
        }
    }
    public <T extends AccessibleObject & Member> MemberInfo(AbstractClassInfo parent,T elt) {
        super(elt);
        this.parent = parent;
        member = elt;
        init();
    }

    public MemberInfo(AbstractClassInfo parent,Element elt) {
        super(elt);
        this.parent = parent;
        element = elt;
        init();
    }


    public List<ModifierInfo> getModifiers(){
        return modifierInfos;
    }

    public boolean isPublic(){
        return modifierInfos.contains(ModifierInfo.PUBLIC);
    }

    public AbstractClassInfo getDeclaringClassInfo(){
        return parent;
    }

    public abstract String getName();

    public abstract String getFullName();

    public Member getMember(){
        return member;
    }
}
