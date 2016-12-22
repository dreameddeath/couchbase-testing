/*
 *
 *  * Copyright Christophe Jeunesse
 *  *
 *  *    Licensed under the Apache License, Version 2.0 (the "License");
 *  *    you may not use this file except in compliance with the License.
 *  *    You may obtain a copy of the License at
 *  *
 *  *      http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  *    Unless required by applicable law or agreed to in writing, software
 *  *    distributed under the License is distributed on an "AS IS" BASIS,
 *  *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  *    See the License for the specific language governing permissions and
 *  *    limitations under the License.
 *
 */

package com.dreameddeath.core.validation;

import com.dreameddeath.core.model.property.Property;
import com.fasterxml.classmate.ResolvedType;
import com.fasterxml.classmate.TypeResolver;
import org.hibernate.validator.spi.valuehandling.ValidatedValueUnwrapper;

import java.lang.reflect.Type;
import java.util.List;

/**
 * Created by Christophe Jeunesse on 24/04/2015.
 */
public class PropertyValueUnwrapper extends ValidatedValueUnwrapper<Property<?>> {

    @Override
    public Object handleValidatedValue(Property<?> value) {
        return value.get();
    }

    @Override
    public Type getValidatedValueType(Type valueType) {
        List<ResolvedType> resolvedTypeList= new TypeResolver().resolve(valueType).typeParametersFor(Property.class);
        return (resolvedTypeList!=null && resolvedTypeList.size()>=1)?resolvedTypeList.get(0).getErasedType():null;
    }
}