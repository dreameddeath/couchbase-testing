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
import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Member;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Christophe Jeunesse on 16/03/2015.
 */
public abstract class MemberInfo extends AnnotatedInfo {
    private AbstractClassInfo _parent;
    private ParameterizedTypeInfo _parameterizedTypeInfo;
    private Member _member;
    private Element _element;
    private List<ModifierInfo> _modifierInfos=new ArrayList<>();

    private void init(){
        if(_element!=null){
            for(javax.lang.model.element.Modifier modifier: _element.getModifiers()){
                ModifierInfo modifierInfo = ModifierInfo.valueOf(modifier);
                if(modifierInfo!=null) {
                    _modifierInfos.add(modifierInfo);
                }
            }
        }
        else{
            int modifier = _member.getModifiers();
            if(Modifier.isPrivate(modifier)){
                _modifierInfos.add(ModifierInfo.PRIVATE);
            }
            else if(Modifier.isPublic(modifier)){
                _modifierInfos.add(ModifierInfo.PUBLIC);
            }
            else if(Modifier.isProtected(modifier)){
                _modifierInfos.add(ModifierInfo.PROTECTED);
            }
        }
    }
    public <T extends AccessibleObject & Member> MemberInfo(AbstractClassInfo parent,T elt) {
        super(elt);
        _parent = parent;
        _member = elt;
        init();
    }

    public MemberInfo(AbstractClassInfo parent,Element elt) {
        super(elt);
        _parent = parent;
        _element = elt;
        init();
    }


    public List<ModifierInfo> getModifiers(){
        return _modifierInfos;
    }

    public boolean isPublic(){
        return _modifierInfos.contains(ModifierInfo.PUBLIC);
    }

    //private


    public AbstractClassInfo getDeclaringClassInfo(){
        return _parent;
    }

    public abstract String getName();

    public abstract String getFullName();

    public Member getMember(){
        return _member;
    }
}
