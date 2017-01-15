/*
 * Copyright Christophe Jeunesse
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */

package com.dreameddeath.core.validation;

import com.dreameddeath.compile.tools.annotation.processor.reflection.ClassInfo;
import com.dreameddeath.core.dao.exception.DaoException;
import com.dreameddeath.core.dao.exception.DuplicateUniqueKeyDaoException;
import com.dreameddeath.core.dao.exception.validation.ValidationFailure;
import com.dreameddeath.core.dao.model.IHasUniqueKeysRef;
import com.dreameddeath.core.model.document.CouchbaseDocument;
import com.dreameddeath.core.model.property.HasParent;
import com.dreameddeath.core.model.util.CouchbaseDocumentFieldReflection;
import com.dreameddeath.core.model.util.CouchbaseDocumentStructureReflection;
import com.dreameddeath.core.validation.annotation.Unique;
import com.dreameddeath.core.validation.exception.ValidationCompositeFailure;
import com.google.common.base.Preconditions;
import io.reactivex.Maybe;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Member;
import java.lang.reflect.Method;

/**
 * Created by Christophe Jeunesse on 06/08/2014.
 */
public class UniqueValidator<T> implements Validator<T> {
    private final Unique annotation;
    private final Field field;
    private final Member[] additionnalFields;

    public UniqueValidator(Field field,Unique ann){
        annotation = ann;
        this.field = field;
        if(ann.additionnalFields().length>0){
            additionnalFields = new Member[annotation.additionnalFields().length];
            if(!CouchbaseDocumentStructureReflection.isReflexible(field.getDeclaringClass())){
                throw new RuntimeException("Cannot have multiple fields for class " + field.getDeclaringClass().getName());
            }
            int pos=0;
            CouchbaseDocumentStructureReflection structureReflection = CouchbaseDocumentStructureReflection.getReflectionFromClassInfo((ClassInfo)ClassInfo.getClassInfo(field.getDeclaringClass()));
            for(String additionnalField : ann.additionnalFields()){
                CouchbaseDocumentFieldReflection fieldReflection = structureReflection.getFieldByPropertyName(additionnalField);
                Preconditions.checkNotNull(fieldReflection,"Cannot find the field {} in class {}",additionnalField,structureReflection.getName());
                additionnalFields[pos]=fieldReflection.getGetter().getMember();
            }
        }
        else{
            additionnalFields = null;
        }
    }

    private String buildFullKey(ValidatorContext ctxt, T value)throws IllegalAccessException,InvocationTargetException{
        if(additionnalFields==null || additionnalFields.length==0){
            return value.toString();
        }
        else{
            StringBuilder sb=new StringBuilder();
            sb.append(value.toString());
            for(Member additionalField:additionnalFields) {
                Object additionnalValue;
                if (additionalField instanceof Field) {
                    additionnalValue = ((Field) additionalField).get(ctxt.head());
                } else {
                    additionnalValue = ((Method) additionalField).invoke(ctxt.head());
                }
                sb.append('|');
                if(additionnalValue!=null){
                    sb.append(additionnalValue.toString());
                }
            }
            return sb.toString();
        }
    }

    @Override
    public Maybe<? extends ValidationFailure> asyncValidate(ValidatorContext ctxt, T value){
        if(value!=null){
            try {
                String valueStr = buildFullKey(ctxt, value);

                CouchbaseDocument root = HasParent.Helper.getParentDocument(ctxt.head());
                String uniqueKey = ctxt.getSession().buildUniqueKey(root, valueStr,annotation.nameSpace());
                if ((root.getBaseMeta() instanceof IHasUniqueKeysRef) && ((IHasUniqueKeysRef) root.getBaseMeta()).isInDbKey(uniqueKey)) {
                    ((IHasUniqueKeysRef) root.getBaseMeta()).addDocUniqKeys(uniqueKey);
                    return Maybe.empty();
                } else {
                    return ctxt.getSession().asyncAddOrUpdateUniqueKey(root, valueStr, annotation.nameSpace())
                            .filter(foundKey -> false)
                            .map(foundKey -> new ValidationCompositeFailure(root, "Shouldn't occur"))
                            .onErrorResumeNext(throwable -> {
                                if (throwable instanceof DuplicateUniqueKeyDaoException) {
                                    return Maybe.just(new ValidationCompositeFailure(ctxt.head(), field, "Duplicate Exception for key <"+((DuplicateUniqueKeyDaoException) throwable).getKey()+">", throwable));
                                }
                                return Maybe.just(new ValidationCompositeFailure(ctxt.head(), field, "Other Exception ["+throwable.getClass().getName()+"/"+throwable.getMessage()+"]", throwable));
                            });
                }
            }
            catch(DaoException e){
                return Maybe.error(e);
            }
            catch(IllegalAccessException|InvocationTargetException e){
                return Maybe.just(new ValidationCompositeFailure(ctxt.head(),field,"Exception during access of secondary values",e));
            }
        }
        return Maybe.empty();
    }
}
