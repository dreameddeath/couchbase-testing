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

package com.dreameddeath.core.validation;

import com.dreameddeath.core.model.annotation.DocumentProperty;
import com.dreameddeath.core.model.document.CouchbaseDocument;
import com.dreameddeath.core.model.property.Property;
import com.dreameddeath.core.model.property.impl.StandardProperty;
import org.hibernate.validator.HibernateValidator;
import org.hibernate.validator.internal.cfg.context.DefaultConstraintMapping;
import org.hibernate.validator.internal.metadata.core.ConstraintHelper;
import org.hibernate.validator.internal.metadata.raw.BeanConfiguration;
import org.hibernate.validator.valuehandling.UnwrapValidatedValue;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.validation.ConstraintViolation;
import javax.validation.ParameterNameProvider;
import javax.validation.Validation;
import javax.validation.ValidatorFactory;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.Set;

/**
 * Created by Christophe Jeunesse on 24/04/2015.
 */
public class PropertyValueUnwrapperTest extends Assert{
    private static Logger LOG = LoggerFactory.getLogger(PropertyValueUnwrapper.class);

    public static class CustomConstraintMapping extends DefaultConstraintMapping{
        public CustomConstraintMapping() {
            super();
        }
        @Override
        public Set<Class<?>> getConfiguredTypes(){
            return super.getConfiguredTypes();
        }

        @Override
        public Set<BeanConfiguration<?>> getBeanConfigurations(ConstraintHelper constraintHelper, ParameterNameProvider parameterNameProvider){
            return super.getBeanConfigurations(constraintHelper, parameterNameProvider);
        }
    }

    public static class TestProperty extends CouchbaseDocument{
        /**
         *  test : Test Property
         */
        @DocumentProperty("test") @Size(min = 3) @NotNull @UnwrapValidatedValue
        private Property<String> _test = new StandardProperty<String>(TestProperty.this);

        // test accessors
        public String getTest() { return _test.get(); }
        public void setTest(String val) { _test.set(val); }
    }

    @Test
    public void testWrapper(){
        ValidatorFactory validatorFactory = Validation
                .byProvider(HibernateValidator.class)
                .configure()
                .addValidatedValueHandler(new PropertyValueUnwrapper())
                .addMapping(new CustomConstraintMapping())
                .buildValidatorFactory();

        TestProperty testObject = new TestProperty();

        Set<ConstraintViolation<TestProperty>> listErrors =  validatorFactory.getValidator().validate(testObject);
        assertEquals(1,listErrors.size());

        testObject.setTest("a not null value");
        Set<ConstraintViolation<TestProperty>> listWithoutErrors =  validatorFactory.getValidator().validate(testObject);
        assertEquals(0, listWithoutErrors.size());
    }

}