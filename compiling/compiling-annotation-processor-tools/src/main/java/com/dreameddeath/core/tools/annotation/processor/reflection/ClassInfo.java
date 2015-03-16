package com.dreameddeath.core.tools.annotation.processor.reflection;

import com.dreameddeath.core.tools.annotation.exception.AnnotationProcessorException;
import com.dreameddeath.core.tools.annotation.processor.AnnotationElementType;

import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by ceaj8230 on 07/03/2015.
 */
public class ClassInfo extends AbstractClassInfo {
    private ClassInfo _superClass=null;
    private List<FieldInfo> _declaredFields = null;

    @Override
    public boolean isInterface() {
        return false;
    }

    private void init(){
        if(getTypeElement()!=null){
            TypeMirror superClass = getTypeElement().getSuperclass();
            if(superClass.getKind()!= TypeKind.NONE){
                _superClass = (ClassInfo)AbstractClassInfo.getClassInfo((TypeElement) ((DeclaredType)superClass).asElement());
            }
        }
        else if(getCurrentClass().getSuperclass()!=null){
            _superClass = (ClassInfo)AbstractClassInfo.getClassInfo(getCurrentClass().getSuperclass());
        }
    }

    protected ClassInfo(TypeElement element){
        super(element);
        init();
    }

    protected ClassInfo(Class<?> clazz){
        super(clazz);
        init();
    }


    public List<FieldInfo> getDeclaredFields(){
        if(_declaredFields ==null){
            _declaredFields =new ArrayList<>();
            if(getTypeElement()!=null){
                for(Element elt:getTypeElement().getEnclosedElements()){
                    try {
                        AnnotationElementType eltType = AnnotationElementType.getTypeOf(elt);
                        if(eltType.equals(AnnotationElementType.FIELD)){
                            _declaredFields.add(new FieldInfo(this,(VariableElement) elt));
                        }
                    }
                    catch(AnnotationProcessorException e){
                        throw new RuntimeException("Unhandled element",e);
                    }
                }
            }
            else{
                for(Field declaredField: getCurrentClass().getDeclaredFields()){
                    _declaredFields.add(new FieldInfo(this,declaredField));
                }
            }
        }
        return Collections.unmodifiableList(_declaredFields);
    }


    public ClassInfo getSuperClass() {
        return _superClass;
    }
}
