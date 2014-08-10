package com.dreameddeath.core.dao.validation;

import com.dreameddeath.core.annotation.DocumentProperty;
import com.dreameddeath.core.annotation.NotNull;
import com.dreameddeath.core.annotation.Unique;
import com.dreameddeath.core.exception.dao.ValidationException;
import com.dreameddeath.core.model.document.CouchbaseDocumentElement;

import java.lang.reflect.*;
import java.util.*;

/**
 * Created by Christophe Jeunesse on 05/08/2014.
 */
public class CouchbaseDocumentElementValidator<T extends CouchbaseDocumentElement> implements Validator<T>{
    Map<AccessibleObject,List<Validator<Object>>> _validationRules = new HashMap<AccessibleObject,List<Validator<Object>>>();
    Validator<Object> _superClassValidator;

    public String fieldGetterName(Field member){
        if(member.getAnnotation(DocumentProperty.class)!=null){
            String getter = member.getAnnotation(DocumentProperty.class).getter();
            if((getter!=null)|| !getter.isEmpty()){
                return getter;
            }
        }
        String name=member.getName();
        if(name.startsWith("_")){
            name = name.substring(1);
        }
        name = name.substring(0,1).toUpperCase()+name.substring(1);
        return "get"+name;
    }
    public AccessibleObject getAccessibleObj(Field member) throws NoSuchMethodException{
        if(!member.isAccessible()){
            String getterName = fieldGetterName(member);
            Method getter = member.getDeclaringClass().getDeclaredMethod(getterName);
            return getter;
        }
        else{
            return member;
        }
    }


    public void addValidator(Field field, Validator validator){
        try {
            AccessibleObject obj = getAccessibleObj(field);

            if (!_validationRules.containsKey(obj)) {
                _validationRules.put(obj, new ArrayList<Validator<Object>>());

            }
            _validationRules.get(obj).add(validator);
        }
        catch(NoSuchMethodException e){
            //TODO manage error
        }
    }

    public CouchbaseDocumentElementValidator(Class<T> rootObj,Map<Class<? extends CouchbaseDocumentElement>,Validator<? extends CouchbaseDocumentElement>> cache){
        for(Field member : rootObj.getDeclaredFields()){
            if(Collection.class.isAssignableFrom(member.getClass())){
                //todo add a foreach constraint
            }
            else{
                if(member.getAnnotation(Unique.class)!=null){
                    addValidator(member,new UniqueValidator(member,member.getAnnotation(Unique.class)));
                }
                if(member.getAnnotation(NotNull.class)!=null){
                    addValidator(member,new NotNullValidator(member));
                }
            }
        }
        if((rootObj.getSuperclass()!=null) && CouchbaseDocumentElement.class.isAssignableFrom(rootObj.getSuperclass())){
            Class<CouchbaseDocumentElement> parentClass = (Class<CouchbaseDocumentElement>)rootObj.getSuperclass();
            if(!cache.containsKey(rootObj.getSuperclass())){
                cache.put(
                        parentClass,
                        new CouchbaseDocumentElementValidator<CouchbaseDocumentElement>(parentClass,cache));
            }
        }
    }

    public void validate(T element,CouchbaseDocumentElement parent) throws ValidationException{
        for(AccessibleObject elt:_validationRules.keySet()){
            Object obj=null;
            try {
                if (elt instanceof Field) {
                    obj = ((Field) elt).get(element);
                } else if (elt instanceof Method) {
                    obj = ((Method) elt).invoke(element);
                }

                for (Validator<Object> validator : _validationRules.get(elt)) {
                    validator.validate(obj,element);
                }
            }
            catch(IllegalAccessException e){

            }
            catch(InvocationTargetException e){

            }
        }
    }

}
