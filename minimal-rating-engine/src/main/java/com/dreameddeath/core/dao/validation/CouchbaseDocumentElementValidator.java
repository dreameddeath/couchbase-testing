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

    public String fieldGetterName(Field field){
        if(field.getAnnotation(DocumentProperty.class)!=null){
            String getter = field.getAnnotation(DocumentProperty.class).getter();
            if((getter!=null)&& !getter.equals("")){
                return getter;
            }
        }
        String name=field.getName();
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
            addSimpleValidator(field,obj,validator);
        }
    }

    private void addValidator(Field field,Validator validator){
        try {
            addValidator(field,getAccessibleObj(field),Iterable.class.isAssignableFrom(field.getDeclaringClass()),validator);
        }
        catch(NoSuchMethodException e){
            //TODO manage error
        }
    }



    public CouchbaseDocumentElementValidator(Class<T> rootObj,ValidatorFactory factory){
        for(Field member : rootObj.getDeclaredFields()){
            if(member.getAnnotation(Validate.class)!=null){
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
        if((rootObj.getSuperclass()!=null) && CouchbaseDocumentElement.class.isAssignableFrom(rootObj.getSuperclass())){
            Class<CouchbaseDocumentElement> parentClass = (Class<CouchbaseDocumentElement>)rootObj.getSuperclass();
            this._validationRules.putAll(((CouchbaseDocumentElementValidator) factory.getValidator(parentClass)).getValidationRules());
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

            }
            catch(InvocationTargetException e){

            }

            if(fieldsErrors!=null){
                throw new ValidationException(element,"has errors",fieldsErrors);
            }
        }
    }

}
