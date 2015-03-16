package com.dreameddeath.core.tools.annotation.processor.reflection;


import javax.lang.model.element.TypeElement;
import javax.lang.model.element.TypeParameterElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by ceaj8230 on 07/03/2015.
 */
public class ParameterizedTypeInfo{
    List<AbstractClassInfo> _parameterizedInfosList=new ArrayList<>();
    List<ParameterizedTypeInfo> _parametersGenericsInfo = new ArrayList<>();

    private void addType(DeclaredType type){
        if(type.asElement().getKind().isInterface()){
            _parameterizedInfosList.add(AbstractClassInfo.getClassInfo((TypeElement)type.asElement()));
        }
        else if(type.asElement().getKind().isClass()){
            _parameterizedInfosList.add(AbstractClassInfo.getClassInfo((TypeElement) type.asElement()));
        }

        for(TypeMirror parameterElement:type.getTypeArguments()){
            _parametersGenericsInfo.add(new ParameterizedTypeInfo(parameterElement));
        }
    }

    private void addType(TypeMirror type) {
        if(type instanceof javax.lang.model.type.WildcardType){
            TypeMirror upperBound = ((javax.lang.model.type.WildcardType) type).getExtendsBound();
            if((upperBound!=null) && (upperBound instanceof DeclaredType)){
                addType((DeclaredType)upperBound);
            }
        }
        else if(type instanceof DeclaredType){
            addType((DeclaredType)type);
        }
    }

    public ParameterizedTypeInfo(TypeParameterElement type) {
        for(TypeMirror subType:type.getBounds()){
            addType(subType);
        }
    }

    public ParameterizedTypeInfo(TypeMirror type){
        addType(type);
    }

    private void addType(Class clazz){
        _parameterizedInfosList.add(AbstractClassInfo.getClassInfo(clazz));
        for(TypeVariable parameterElement:clazz.getTypeParameters()){
            _parametersGenericsInfo.add(new ParameterizedTypeInfo(parameterElement));
        }
    }

    public ParameterizedTypeInfo(Type paramType){
        if(paramType instanceof Class){
            addType((Class)paramType);
        }
        else if(paramType instanceof TypeVariable){
            for(Type parentType :((java.lang.reflect.TypeVariable)paramType).getBounds()){
                if(parentType instanceof Class){
                    addType((Class)parentType);
                }
            }
        }
        else if(paramType instanceof java.lang.reflect.WildcardType){
            for(Type parentType:((java.lang.reflect.WildcardType)paramType).getUpperBounds()){
                if(parentType instanceof Class){
                    addType((Class)parentType);
                }
            }
        }
        else if(paramType instanceof ParameterizedType) {
            ParameterizedType parameterizedType = (ParameterizedType)paramType;
            ParameterizedTypeInfo rawTypeInfo = new ParameterizedTypeInfo(parameterizedType.getRawType());
            _parameterizedInfosList.addAll(rawTypeInfo._parameterizedInfosList);
            for(Type genericParam:parameterizedType.getActualTypeArguments()){
                _parametersGenericsInfo.add(new ParameterizedTypeInfo(genericParam));
            }
        }

    }

    public List<AbstractClassInfo> getParameterizedInfosList() {
        return _parameterizedInfosList;
    }

    public AbstractClassInfo getMainType(){
        return _parameterizedInfosList.get(0);
    }

    public ParameterizedTypeInfo getMainTypeGeneric(int pos){
        return _parametersGenericsInfo.get(pos);
    }


    public boolean isAssignableTo(Class clazz){
        return AbstractClassInfo.getClassInfo(clazz).isAssignableFrom(getMainType());
    }
}
