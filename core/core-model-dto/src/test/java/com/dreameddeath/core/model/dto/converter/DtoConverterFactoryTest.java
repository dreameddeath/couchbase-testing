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

package com.dreameddeath.core.model.dto.converter;

import com.dreameddeath.core.json.ObjectMapperFactory;
import com.dreameddeath.core.model.dto.annotation.processor.model.AbstractTestingModel;
import com.dreameddeath.core.model.dto.annotation.processor.model.TestingModelInherited;
import com.dreameddeath.core.model.dto.annotation.processor.model.converter.TestingModelInheritedInputInputV1_0Converter;
import com.dreameddeath.core.model.dto.annotation.processor.model.converter.TestingModelInheritedOutputOutputV1_0Converter;
import com.dreameddeath.core.model.dto.annotation.processor.model.published.AbstractTestingModelInput;
import com.dreameddeath.core.model.dto.annotation.processor.model.published.AbstractTestingModelOutput;
import com.dreameddeath.core.model.dto.annotation.processor.model.published.TestingModelInheritedInput;
import com.dreameddeath.core.model.dto.annotation.processor.model.published.TestingModelInheritedOutput;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Created by CEAJ8230 on 28/05/2017.
 */
public class DtoConverterFactoryTest {
    @Test
    public void testFactory() throws Exception{
        DtoConverterFactory factory = new DtoConverterFactory();
        ObjectMapper mapper = ObjectMapperFactory.BASE_INSTANCE.getMapper();
        {
            //InputChecks
            IDtoInputConverter<TestingModelInherited, TestingModelInheritedInput> dtoInputConverter = factory.getDtoInputConverter(TestingModelInherited.class, TestingModelInheritedInput.class);
            IDtoInputConverter<AbstractTestingModel, AbstractTestingModelInput> dtoInputGenericConverter = factory.getDtoInputConverter(AbstractTestingModel.class,AbstractTestingModelInput.class);
            assertEquals(dtoInputConverter.getClass(), TestingModelInheritedInputInputV1_0Converter.class);
            assertEquals(dtoInputGenericConverter.getClass(), DtoGenericInputAbstractConverter.class);
            TestingModelInheritedInput modelInheritedInput = new TestingModelInheritedInput();
            modelInheritedInput.setLongValue(100L);
            modelInheritedInput.setStrValue("test");
            modelInheritedInput.setInheritedStrValue("test");

            TestingModelInherited simpleMapping = dtoInputConverter.convertToDoc(modelInheritedInput);
            AbstractTestingModel genericMapping = dtoInputGenericConverter.convertToDoc(modelInheritedInput);

            assertEquals(TestingModelInherited.class, genericMapping.getClass());
            assertEquals(modelInheritedInput.getStrValue(), simpleMapping.strValue);
            assertEquals(modelInheritedInput.getLongValue(), simpleMapping.longValue);
            assertEquals(modelInheritedInput.getInheritedStrValue(), simpleMapping.inheritedStrValue);
            assertEquals(modelInheritedInput.getStrValue(), genericMapping.strValue);
            assertEquals(modelInheritedInput.getLongValue(), ((TestingModelInherited)genericMapping).longValue);
            assertEquals(modelInheritedInput.getInheritedStrValue(), ((TestingModelInherited)genericMapping).inheritedStrValue);

            byte[] converterValue = mapper.writeValueAsBytes(modelInheritedInput);
            AbstractTestingModelInput jsonMappedValue = mapper.readValue(converterValue,AbstractTestingModelInput.class);
            assertEquals(TestingModelInheritedInput.class, jsonMappedValue.getClass());
            assertEquals(modelInheritedInput.getStrValue(), jsonMappedValue.getStrValue());
            assertEquals(modelInheritedInput.getLongValue(), ((TestingModelInheritedInput)jsonMappedValue).getLongValue());
            assertEquals(modelInheritedInput.getInheritedStrValue(), ((TestingModelInheritedInput)jsonMappedValue).getInheritedStrValue());
        }

        {
            //Output checks
            IDtoOutputConverter<TestingModelInherited, TestingModelInheritedOutput> dtoOutputConverter = factory.getDtoOutputConverter(TestingModelInherited.class, TestingModelInheritedOutput.class);
            assertEquals(dtoOutputConverter.getClass(), TestingModelInheritedOutputOutputV1_0Converter.class);

            TestingModelInherited modelToConvert = new TestingModelInherited();
            modelToConvert.inheritedStrValue = "inheritedTest";
            modelToConvert.strValue = "test";
            modelToConvert.longValue = 100L;

            TestingModelInheritedOutput output = dtoOutputConverter.convertToOutput(modelToConvert);
            assertEquals(modelToConvert.inheritedStrValue, output.getInheritedStrValue());
            assertEquals(modelToConvert.strValue, output.getStrValue());
            assertEquals(modelToConvert.longValue, output.getLongValue());

            byte[] converterValue = mapper.writeValueAsBytes(output);
            AbstractTestingModelOutput outputFromJson = mapper.readValue(converterValue, AbstractTestingModelOutput.class);
            assertTrue(outputFromJson instanceof TestingModelInheritedOutput);
            assertEquals(modelToConvert.inheritedStrValue, ((TestingModelInheritedOutput) outputFromJson).getInheritedStrValue());
            assertEquals(modelToConvert.strValue, outputFromJson.getStrValue());
            assertEquals(modelToConvert.longValue, ((TestingModelInheritedOutput) outputFromJson).getLongValue());

            IDtoOutputConverter<AbstractTestingModel, AbstractTestingModelOutput> abstractDtoOutputConverter = factory.getDtoOutputConverter(AbstractTestingModel.class, AbstractTestingModelOutput.class);
            AbstractTestingModelOutput abstractOutput = abstractDtoOutputConverter.convertToOutput(modelToConvert);
            assertTrue(abstractOutput instanceof TestingModelInheritedOutput);
            assertEquals(modelToConvert.strValue, abstractOutput.getStrValue());
            assertEquals(modelToConvert.inheritedStrValue, ((TestingModelInheritedOutput) abstractOutput).getInheritedStrValue());
            assertEquals(modelToConvert.longValue, ((TestingModelInheritedOutput) abstractOutput).getLongValue());
        }
    }
}