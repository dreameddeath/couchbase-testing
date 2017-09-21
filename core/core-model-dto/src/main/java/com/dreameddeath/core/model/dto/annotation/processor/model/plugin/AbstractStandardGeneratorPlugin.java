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

package com.dreameddeath.core.model.dto.annotation.processor.model.plugin;

import com.dreameddeath.compile.tools.annotation.processor.reflection.ClassInfo;
import com.dreameddeath.compile.tools.annotation.processor.reflection.FieldInfo;
import com.dreameddeath.compile.tools.annotation.processor.reflection.MemberInfo;
import com.dreameddeath.core.java.utils.StringUtils;
import com.dreameddeath.core.model.dto.annotation.DtoInOutMode;
import com.dreameddeath.core.model.dto.annotation.DtoModelJsonTypeId;
import com.dreameddeath.core.model.dto.annotation.processor.model.AbstractDtoModelGenerator;
import com.dreameddeath.core.model.dto.annotation.processor.model.FieldGenMode;
import com.dreameddeath.core.model.dto.annotation.processor.model.Key;
import com.dreameddeath.core.model.dto.annotation.processor.model.SuperClassGenMode;
import com.dreameddeath.core.model.dto.json.DtoModelTypeIdResolver;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.annotation.JsonTypeIdResolver;
import com.google.common.collect.Lists;
import com.squareup.javapoet.AnnotationSpec;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.TypeSpec;

import java.lang.annotation.Annotation;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

/**
 * Created by Christophe Jeunesse on 27/06/2017.
 */
public abstract class AbstractStandardGeneratorPlugin<TROOT extends Annotation,TINFIELD extends Annotation,TOUTFIELD extends Annotation> implements  IDtoModelGeneratorPlugin{
    protected abstract String getDtoModelType();
    protected abstract Class<TROOT> getRootAnnot();
    protected abstract Class<TINFIELD> getFieldInputAnnot();
    protected abstract Class<TOUTFIELD> getFieldOutputAnnot();

    protected abstract Map<DtoInOutMode,String> classSuffixPerMode();
    protected abstract SuperClassGenMode getDefaultSuperClassMode();
    protected abstract FieldGenMode getDefaultOutFieldGenMode();
    protected abstract FieldGenMode getDefaultInFieldGenMode();



    protected abstract String getRootAnnotVersion(TROOT rootAnnot);
    protected abstract SuperClassGenMode getRootAnnotSuperClassGenMode(TROOT rootAnnot);
    protected abstract String getRootAnnotJsonTypeId(TROOT rootAnnot);
    protected abstract boolean getRootAnnotIsClassHierarchyRoot(TROOT rootAnnot);
    protected abstract FieldGenMode getRootAnnotDefaultOutputFieldMode(TROOT rootAnnot);
    protected abstract FieldGenMode getRootAnnotDefaultInputFieldMode(TROOT rootAnnot);

    protected abstract String getInFieldAnnotVersion(TINFIELD fieldAnnot);
    protected abstract FieldGenMode getInFieldAnnotMode(TINFIELD fieldAnnot);
    protected abstract FieldGenMode getInFieldAnnotUnwrappedDefaultMode(TINFIELD fieldAnnot);
    protected abstract String getInFieldAnnotFieldName(TINFIELD fieldAnnot);

    protected abstract String getOutFieldAnnotVersion(TOUTFIELD fieldAnnot);
    protected abstract FieldGenMode getOutFieldAnnotMode(TOUTFIELD fieldAnnot);
    protected abstract FieldGenMode getOutFieldAnnotUnwrappedDefaultMode(TOUTFIELD fieldAnnot);
    protected abstract String getOutFieldAnnotFieldName(TOUTFIELD fieldAnnot);

    protected abstract void generateForAnnot(AbstractDtoModelGenerator abstractDtoModelGenerator, ClassInfo entityClassInfo, TROOT annot);

    @Override
    public Key getKey(ClassInfo clazz, DtoInOutMode mode, String type, String version) {
        if(!getDtoModelType().equals(type)){
            return null;
        }
        String packageName = clazz.getPackageInfo().getName()+".published."+getDtoModelType().toLowerCase();

        String className=clazz.getSimpleName().replaceAll("\\$","");
        if(!classSuffixPerMode().containsKey(mode)){
            throw new IllegalStateException("The mode "+mode+" isn't allowed for type "+getDtoModelType());
        }
        else{
            className+=classSuffixPerMode().get(mode);
        }
        return new Key(packageName,className,mode,type,version);
    }

    @Override
    public boolean isApplicableToKey(Key dtoModelKey) {
        return dtoModelKey.getType().equals(getDtoModelType());
    }

    protected TROOT[] getRootAnnotForSuperClass(ClassInfo classInfo){
        if(classInfo.getSuperClass()!=null){
            ClassInfo superClass = classInfo.getSuperClass();
            TROOT[] annots = superClass.getAnnotationByType(getRootAnnot());
            if(annots!=null && annots.length>0){
                return annots;
            }
            return getRootAnnotForSuperClass(superClass);
        }
        return null;
    }

    protected TROOT getRootAnnotForKey(ClassInfo clazz, Key key, List<AbstractDtoModelGenerator.UnwrappingStackElement> unwrappingStackElements, boolean lookOnSuperClass) {
        if(unwrappingStackElements!=null && unwrappingStackElements.size()>0){
            for(AbstractDtoModelGenerator.UnwrappingStackElement element: Lists.reverse(unwrappingStackElements)){
                if(!element.isForSuperclass()){
                    TROOT annot = getRootAnnotForKey(element.getFieldInfo().getDeclaringClassInfo(),key, Collections.emptyList(),false);
                    if(annot!=null){
                        return annot;
                    }
                }
            }
        }

        TROOT[] annots = clazz.getAnnotationByType(getRootAnnot());
        if((annots==null ||annots.length==0) && lookOnSuperClass){
            annots= getRootAnnotForSuperClass(clazz);
        }
        if (annots != null && annots.length > 0) {
            for (TROOT annot : annots) {
                if (StringUtils.isEmpty(getRootAnnotVersion(annot)) || getRootAnnotVersion(annot).equals(key.getVersion())) {
                    return annot;
                }
            }
        }
        return null;
    }

    @Override
    public void generateIfNeeded(AbstractDtoModelGenerator abstractDtoModelGenerator, ClassInfo entityClassInfo) {
        TROOT[] annots = entityClassInfo.getAnnotationByType(getRootAnnot());
        if(annots==null || annots.length==0){
            annots= getRootAnnotForSuperClass(entityClassInfo);
        }
        if(annots!=null && annots.length>0){
            for(TROOT annot : annots){
                if(! entityClassInfo.isAbstract() || forceGenerateAbstractClass(entityClassInfo,annot)) {
                    generateForAnnot(abstractDtoModelGenerator, entityClassInfo, annot);
                }
            }
        }
    }

    protected boolean forceGenerateAbstractClass(ClassInfo entityClassInfo, TROOT annot){
        return false;
    }


    @Override
    public SuperClassGenMode getSuperClassGeneratorMode(ClassInfo childClass, ClassInfo parentClazz, Key dtoModelKey, List<AbstractDtoModelGenerator.UnwrappingStackElement> unwrappingStackElements) {
        TROOT annot = getRootAnnotForKey(childClass,dtoModelKey,unwrappingStackElements,false);
        if(annot!=null){
            return getRootAnnotSuperClassGenMode(annot);
        }
        else{
            //If parent has the plugin dto model annot, force generate in auto mode
            TROOT parentAnnot = getRootAnnotForKey(parentClazz,dtoModelKey,unwrappingStackElements,false);
            if(parentAnnot!=null){
                //SuperClassGenMode defaultSuperClassGenMode = getDefaultSuperClassMode();
                if(parentClazz.isAbstract()){
                    return forceGenerateAbstractClass(parentClazz,parentAnnot)?SuperClassGenMode.AUTO:SuperClassGenMode.UNWRAP;
                }
                return SuperClassGenMode.AUTO;
            }
            else if(parentClazz.getSuperClass()!=null){
                return getSuperClassGeneratorMode(parentClazz,parentClazz.getSuperClass(),dtoModelKey,unwrappingStackElements);
            }
        }
        return getDefaultSuperClassMode();
    }


    private <T extends Annotation> T getFieldAnnotForKey(MemberInfo fieldInfo, Key key, Class<T> annotClass, Function<T,String> versionGetter) {
        T[] annots = fieldInfo.getAnnotationByType(annotClass);
        if(annots!=null && annots.length>0) {
            for (T annot : annots) {
                if (StringUtils.isEmpty(versionGetter.apply(annot)) || versionGetter.apply(annot).equals(key.getVersion())) {
                    return annot;
                }
            }
        }
        return null;
    }


    private <T extends Annotation> T getFieldAnnotForKey(AbstractDtoModelGenerator.SourceInfoForField fieldInfo, Key key, Class<T> annotClass, Function<T,String> versionGetter) {
        List<T> annots = fieldInfo.getAnnotations(annotClass);
        for (T annot : annots) {
            if (StringUtils.isEmpty(versionGetter.apply(annot)) || versionGetter.apply(annot).equals(key.getVersion())) {
                return annot;
            }
        }
        return null;
    }



    @Override
    public FieldGenMode getFieldGeneratorMode(FieldInfo field, Key dtoModelKey, List<AbstractDtoModelGenerator.UnwrappingStackElement> unwrappingStackElements) {
        boolean isInput = dtoModelKey.getInOutMode() == DtoInOutMode.IN;
        if(isInput){
            TINFIELD annot = getFieldAnnotForKey(field,dtoModelKey,getFieldInputAnnot(),this::getInFieldAnnotVersion);
            if(annot!=null){ return getInFieldAnnotMode(annot);}
        }
        else{
            TOUTFIELD annot = getFieldAnnotForKey(field,dtoModelKey,getFieldOutputAnnot(),this::getOutFieldAnnotVersion);
            if(annot!=null){return getOutFieldAnnotMode(annot);}
        }
        return null;
    }


    @Override
    public FieldGenMode getUnwrappedFieldGenMode(AbstractDtoModelGenerator.SourceInfoForField field, Key dtoModelKey, List<AbstractDtoModelGenerator.UnwrappingStackElement> unwrappingStackElements) {
        boolean isInput = dtoModelKey.getInOutMode() == DtoInOutMode.IN;
        if(isInput){
            TINFIELD annot = getFieldAnnotForKey(field,dtoModelKey,getFieldInputAnnot(),this::getInFieldAnnotVersion);
            if(annot!=null){ return getInFieldAnnotUnwrappedDefaultMode(annot);}
        }
        else{
            TOUTFIELD annot = getFieldAnnotForKey(field,dtoModelKey,getFieldOutputAnnot(),this::getOutFieldAnnotVersion);
            if(annot!=null){return getOutFieldAnnotUnwrappedDefaultMode(annot);}
        }

        return FieldGenMode.INHERIT;
    }

    @Override
    public String getFieldEffectiveName(FieldInfo field, Key dtoModelKey, List<AbstractDtoModelGenerator.UnwrappingStackElement> unwrappingStackElements) {
        boolean isInput = dtoModelKey.getInOutMode() == DtoInOutMode.IN;
        if(isInput){
            TINFIELD annot = getFieldAnnotForKey(field,dtoModelKey,getFieldInputAnnot(),this::getInFieldAnnotVersion);
            if(annot!=null && StringUtils.isNotEmpty(getInFieldAnnotFieldName(annot))){return getInFieldAnnotFieldName(annot);}
        }
        else{
            TOUTFIELD annot = getFieldAnnotForKey(field,dtoModelKey,getFieldOutputAnnot(),this::getOutFieldAnnotVersion);
            if(annot!=null && StringUtils.isNotEmpty(getOutFieldAnnotFieldName(annot))){return getOutFieldAnnotFieldName(annot);}
        }
        return null;
    }

    @Override
    public void addHierarchyBasedTypeInfo(TypeSpec.Builder typeBuilder, ClassInfo clazz, Key key, ClassName superClassDtoName) {
        TROOT annot = getRootAnnotForKey(clazz,key,null,false);
        boolean isRootHierachy = annot != null && getRootAnnotIsClassHierarchyRoot(annot);
        if(((superClassDtoName!=null || isRootHierachy) && !clazz.isAbstract())) {
            String jsonTypeId = null;
            if(annot!=null){
                jsonTypeId = getRootAnnotJsonTypeId(annot);
            }
            if (StringUtils.isEmpty(jsonTypeId)) {
                jsonTypeId = key.getClassName();
            }
            typeBuilder.addAnnotation(
                    AnnotationSpec.builder(DtoModelJsonTypeId.class)
                            .addMember("value", "$S", jsonTypeId)
                            .build()
            );
        }
        //Manage root class
        if(isRootHierachy ||(clazz.isAbstract() && superClassDtoName==null)){
            typeBuilder.addAnnotation(
                    AnnotationSpec.builder(JsonTypeInfo.class)
                            .addMember("use","$T.$L",JsonTypeInfo.Id.class,JsonTypeInfo.Id.CUSTOM.name())
                            .addMember("include","$T.$L",JsonTypeInfo.As.class,JsonTypeInfo.As.PROPERTY.name())
                            .addMember("property","$S","@t")
                            //.addMember("visible","$L","true")
                            .build()
            );

            typeBuilder.addAnnotation(
                    AnnotationSpec.builder(JsonTypeIdResolver.class)
                            .addMember("value", "$T.class", DtoModelTypeIdResolver.class)
                            .build()
            );
        }
    }

    @Override
    public FieldGenMode getFieldGeneratorModeDefaultFromClass(ClassInfo rootClassInfo, Key dtoModelKey, List<AbstractDtoModelGenerator.UnwrappingStackElement> unwrappingStackElements) {
        boolean isInput = dtoModelKey.getInOutMode() == DtoInOutMode.IN;
        TROOT rootAnnotForKey = getRootAnnotForKey(rootClassInfo,dtoModelKey,unwrappingStackElements,true);
        if(rootAnnotForKey!=null) {
            if (isInput) {
                return getRootAnnotDefaultInputFieldMode(rootAnnotForKey);
            }
            else{
                return getRootAnnotDefaultOutputFieldMode(rootAnnotForKey);
            }
        }
        return isInput?getDefaultInFieldGenMode():getDefaultOutFieldGenMode();
    }

    @Override
    public Collection<String> getSupportedAnnotations() {
        return Collections.singletonList(getRootAnnot().getCanonicalName());
    }
}
