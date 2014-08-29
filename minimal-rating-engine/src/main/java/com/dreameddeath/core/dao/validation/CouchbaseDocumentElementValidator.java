package com.dreameddeath.core.dao.validation;

import com.dreameddeath.core.annotation.*;
import com.dreameddeath.core.exception.dao.ValidationException;
import com.dreameddeath.core.model.document.CouchbaseDocumentElement;

import java.lang.reflect.*;
import java.util.*;

/**
 * Created by Christophe Jeunesse on 05/08/2014.
 */
public class CouchbaseDocumentElementValidator<T extends CouchbaseDocumentElement> implements Validator<T>{
    Map<AccessibleObject,List<Validator>> _validationRules = new HashMap<AccessibleObject,List<Validator>>();
    public Map<AccessibleObject,List<Validator>> getValidationRules(){
        return Collections.unmodifiableMap(_validationRules);
    }
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


    private void addSimpleValidator(AccessibleObject obj,Validator validator){
        if (!_validationRules.containsKey(obj)) {
            _validationRules.put(obj, new ArrayList<Validator>());
        }
        _validationRules.get(obj).add(validator);
    }

    private void addIterableValidator(AccessibleObject obj,Validator validator){
        if (!_validationRules.containsKey(obj)) {
            _validationRules.put(obj, new ArrayList<Validator>());
        }

        for(Validator existingValidator:_validationRules.get(obj)){
            if(existingValidator instanceof IterableValidator){
                ((IterableValidator) existingValidator).addRule(validator);
                return;
            }
        }
        IterableValidator newIterableValidator = new IterableValidator((Member)obj);
        newIterableValidator.addRule(validator);
        _validationRules.get(obj).add(newIterableValidator);
    }

    private void addValidator(AccessibleObject obj,boolean isIterable,Validator validator){
        if(isIterable){
            addIterableValidator(obj,validator);
        }
        else{
            addSimpleValidator(obj,validator);
        }
    }

    private void addValidator(Field field,Validator validator){
        try {
            addValidator(getAccessibleObj(field),Iterable.class.isAssignableFrom(field.getDeclaringClass()),validator);
        }
        catch(NoSuchMethodException e){
            //TODO manage error
        }
    }

    private void addValidator(CouchbaseDocumentElementValidator<? extends CouchbaseDocumentElement> srcValidator){
        for(Map.Entry<AccessibleObject,List<Validator>> fieldValidators :
                srcValidator.getValidationRules().entrySet()){
            for(Validator<Object> validator:fieldValidators.getValue()){
                addValidator(fieldValidators.getKey(),false,validator);
            }
        }
    }

    public CouchbaseDocumentElementValidator(Class<T> rootObj,Map<Class<? extends CouchbaseDocumentElement>,Validator<? extends CouchbaseDocumentElement>> cache){
        for(Field member : rootObj.getDeclaredFields()){
            if(member.getAnnotation(Validate.class)!=null){
                addValidator(member,new DynamicCouchbaseDocumentElementValidator(member,cache));
            }

            if(member.getAnnotation(Unique.class)!=null){
                addValidator(member,new UniqueValidator(member,member.getAnnotation(Unique.class)));
            }
            if(member.getAnnotation(NotNull.class)!=null){
                addValidator(member,new NotNullValidator(member,member.getAnnotation(NotNull.class)));
            }
            if(member.getAnnotation(ValidationConstraint.class)!=null){
                ValidationConstraint ann=member.getAnnotation(ValidationConstraint.class);
                Class[] argTypes={Field.class,ValidationConstraint.class};
                Object[] args={member,ann};
                try {
                    addValidator(member, ann.validationClass().getConstructor(argTypes).newInstance(args));
                }
                catch(NoSuchMethodException e) {
                    //TODO throw error
                }
                catch(InstantiationException e) {
                    //TODO throw error
                }
                catch(IllegalAccessException e) {
                    //TODO throw error
                }
                catch(InvocationTargetException e) {
                    //TODO throw error
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
        List<ValidationException> fieldsErrors=null;
        for(AccessibleObject elt:_validationRules.keySet()){
            Object obj=null;
            try {
                if (elt instanceof Field) {
                    obj = ((Field) elt).get(element);
                } else if (elt instanceof Method) {
                    obj = ((Method) elt).invoke(element);
                }

                List<ValidationException> fldErrors=null;
                for (Validator<Object> validator : _validationRules.get(elt)) {
                    try {
                        validator.validate(obj, element);
                    }
                    catch(ValidationException e){
                        if(fldErrors==null){
                            fldErrors = new ArrayList<ValidationException>();
                        }
                        fldErrors.add(e);
                    }
                }
                if(fldErrors!=null){
                    if(fieldsErrors==null){
                        fieldsErrors=new ArrayList<ValidationException>();
                    }
                    fieldsErrors.add(new ValidationException(element,elt,"Errors of field",fldErrors));
                }
            }
            catch(IllegalAccessException e){

            }
            catch(InvocationTargetException e){

            }

            if(fieldsErrors!=null){
                throw new ValidationException(parent,elt,"Errors of element",fieldsErrors);
            }
        }
    }

}
