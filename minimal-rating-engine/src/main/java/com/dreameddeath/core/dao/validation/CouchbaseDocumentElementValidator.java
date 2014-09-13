package com.dreameddeath.core.dao.validation;

import com.dreameddeath.core.annotation.*;
import com.dreameddeath.core.exception.dao.ValidationException;
import com.dreameddeath.core.model.common.BaseCouchbaseDocumentElement;
import com.dreameddeath.core.model.document.CouchbaseDocument;
import com.dreameddeath.core.model.property.Property;

import java.lang.reflect.*;
import java.util.*;

/**
 * Created by Christophe Jeunesse on 05/08/2014.
 */
public class CouchbaseDocumentElementValidator<T extends BaseCouchbaseDocumentElement> implements Validator<T>{
    Map<AccessibleObject,CouchbaseDocumentValidatorFieldEntry> _validationRules = new HashMap<AccessibleObject,CouchbaseDocumentValidatorFieldEntry>();

    public static class CouchbaseDocumentValidatorFieldEntry{
        private List<Validator> _validationRules=new ArrayList<Validator>();
        private String _fieldName;
        private Field _field;

        public CouchbaseDocumentValidatorFieldEntry(Field field){
            _field = field;
            DocumentProperty docProp = field.getAnnotation(DocumentProperty.class);
            if(docProp!=null){
                _fieldName=docProp.value();
            }
            else{
                _fieldName=field.getName();
            }
        }

        public String getFieldName(){
            return _fieldName;
        }

        public Field getField(){
            return _field;
        }

        public List<Validator> getValidators(){
            return Collections.unmodifiableList(_validationRules);
        }
        public void addValidator(Validator validator){
            _validationRules.add(validator);
        }
    }

    public Map<AccessibleObject,CouchbaseDocumentValidatorFieldEntry> getValidationRules(){
        return Collections.unmodifiableMap(_validationRules);
    }

    public Method fieldGetterFinder(Field field) throws NoSuchMethodException{
        if(field.getAnnotation(DocumentProperty.class)!=null){
            DocumentProperty prop = field.getAnnotation(DocumentProperty.class);
            String getter = prop.getter();
            if((getter!=null)&& !getter.equals("")){
                return field.getDeclaringClass().getDeclaredMethod(getter);
            }
            else {
                String name = prop.value();
                name = "get"+name.substring(0,1).toUpperCase()+name.substring(1);
                try {
                    return field.getDeclaringClass().getDeclaredMethod(name);
                }
                catch(NoSuchMethodException e){
                    //Do nothing
                }
            }
        }

        String name=field.getName();
        if(name.startsWith("_")){
            name = name.substring(1);
        }
        name = "get"+name.substring(0,1).toUpperCase()+name.substring(1);
        return field.getDeclaringClass().getDeclaredMethod(name);

    }

    public AccessibleObject getAccessibleObj(Field member) throws NoSuchMethodException{
        if(!member.isAccessible()){
            return fieldGetterFinder(member);
        }
        else{
            return member;
        }
    }

    private void addPropertyValidator(Field field,AccessibleObject obj,Validator validator){
        if (!_validationRules.containsKey(obj)) {
            _validationRules.put(obj, new CouchbaseDocumentValidatorFieldEntry(field));
        }

        for(Validator existingValidator:_validationRules.get(obj).getValidators()){
            if(existingValidator instanceof PropertyValidator){
                ((PropertyValidator) existingValidator).addRule(validator);
                return;
            }
        }
        PropertyValidator newPropertyValidator = new PropertyValidator((Member)obj);
        newPropertyValidator.addRule(validator);
        _validationRules.get(obj).addValidator(newPropertyValidator);
    }


    private void addSimpleValidator(Field field,AccessibleObject obj,Validator validator){
        if (!_validationRules.containsKey(obj)) {
            _validationRules.put(obj,new CouchbaseDocumentValidatorFieldEntry(field));
        }
        _validationRules.get(obj).addValidator(validator);
    }

    private void addIterableValidator(Field field,AccessibleObject obj,Validator validator){
        if (!_validationRules.containsKey(obj)) {
            _validationRules.put(obj, new CouchbaseDocumentValidatorFieldEntry(field));
        }

        for(Validator existingValidator:_validationRules.get(obj).getValidators()){
            if(existingValidator instanceof IterableValidator){
                ((IterableValidator) existingValidator).addRule(validator);
                return;
            }
        }
        IterableValidator newIterableValidator = new IterableValidator((Member)obj);
        newIterableValidator.addRule(validator);
        _validationRules.get(obj).addValidator(newIterableValidator);
    }

    private void addValidator(Field field,AccessibleObject obj,boolean isIterable,Validator validator){
        if(isIterable){
            addIterableValidator(field,obj,validator);
        }
        else{
            Class<?> returnClass;
            if(obj instanceof Method){
                returnClass = ((Method)obj).getReturnType();
            }
            else{
                returnClass = ((Field)obj).getType();
            }

            if(Property.class.isAssignableFrom(returnClass)){
                addPropertyValidator(field, obj, validator);
            }
            else {
                addSimpleValidator(field, obj, validator);
            }
        }
    }

    private void addValidator(Field field,Validator validator){
        try {
            addValidator(field,getAccessibleObj(field),Iterable.class.isAssignableFrom(field.getType()),validator);
        }
        catch(NoSuchMethodException e){
            throw new RuntimeException("Cannot find the getter of field "+field.getName(),e);
        }
    }



    public CouchbaseDocumentElementValidator(Class<T> rootObj,ValidatorFactory factory){
        for(Field member : rootObj.getDeclaredFields()){
            if(member.getAnnotation(Validate.class)!=null){
                addValidator(member,new DynamicCouchbaseDocumentElementValidator(member,factory));
            }
            else if(
                    (member.getAnnotation(DocumentProperty.class)!=null) &&
                    (
                        (CouchbaseDocument.class.isAssignableFrom(member.getType()))||
                        (
                            Property.class.isAssignableFrom(member.getType()) &&
                            (
                                (((ParameterizedType)member.getGenericType()).getActualTypeArguments()[0] instanceof TypeVariable) ||
                                BaseCouchbaseDocumentElement.class.isAssignableFrom((Class<?>)((ParameterizedType)member.getGenericType()).getActualTypeArguments()[0])
                            )
                        )
                    )
                )
            {
                addValidator(member,new DynamicCouchbaseDocumentElementValidator(member,factory));
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
        if((rootObj.getSuperclass()!=null) && BaseCouchbaseDocumentElement.class.isAssignableFrom(rootObj.getSuperclass())){
            Class<BaseCouchbaseDocumentElement> parentClass = (Class<BaseCouchbaseDocumentElement>)rootObj.getSuperclass();
            this._validationRules.putAll(((CouchbaseDocumentElementValidator) factory.getValidator(parentClass)).getValidationRules());
        }
    }

    public void validate(T element,BaseCouchbaseDocumentElement parent) throws ValidationException{
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
                for (Validator<Object> validator : _validationRules.get(elt).getValidators()) {
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
                    fieldsErrors.add(new ValidationException(element,_validationRules.get(elt).getField(),"Errors of field",fldErrors));
                }
            }
            catch(IllegalAccessException e){
                if(fieldsErrors==null){
                    fieldsErrors=new ArrayList<ValidationException>();
                }
                fieldsErrors.add(new ValidationException(element,_validationRules.get(elt).getField(),"Cannot access to the value of the field",e));
            }
            catch(InvocationTargetException e){
                if(fieldsErrors==null){
                    fieldsErrors=new ArrayList<ValidationException>();
                }
                fieldsErrors.add(new ValidationException(element,_validationRules.get(elt).getField(),"Cannot access to the target of the field",e));
            }

            if(fieldsErrors!=null){
                throw new ValidationException(element,"has errors",fieldsErrors);
            }
        }
    }

}
