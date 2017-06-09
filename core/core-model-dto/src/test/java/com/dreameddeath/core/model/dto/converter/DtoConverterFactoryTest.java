package com.dreameddeath.core.model.dto.converter;

import com.dreameddeath.core.model.dto.annotation.processor.model.AbstractTestingModel;
import com.dreameddeath.core.model.dto.annotation.processor.model.TestingModelInherited;
import com.dreameddeath.core.model.dto.annotation.processor.model.converter.TestingModelInheritedOutputOutputV1_0Converter;
import com.dreameddeath.core.model.dto.annotation.processor.model.published.AbstractTestingModelOutput;
import com.dreameddeath.core.model.dto.annotation.processor.model.published.TestingModelInheritedOutput;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Created by CEAJ8230 on 28/05/2017.
 */
public class DtoConverterFactoryTest {
    @Test
    public void testFactory(){
        DtoConverterFactory factory = new DtoConverterFactory();
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

        IDtoOutputConverter<AbstractTestingModel, AbstractTestingModelOutput> abstractDtoOutputConverter = factory.getDtoOutputConverter(AbstractTestingModel.class, AbstractTestingModelOutput.class);
        AbstractTestingModelOutput abstractOutput = abstractDtoOutputConverter .convertToOutput(modelToConvert);
        assertTrue(abstractOutput instanceof TestingModelInheritedOutput);
        assertEquals(modelToConvert.strValue, abstractOutput.getStrValue());
        assertEquals(modelToConvert.inheritedStrValue, ((TestingModelInheritedOutput)abstractOutput).getInheritedStrValue());
        assertEquals(modelToConvert.longValue, ((TestingModelInheritedOutput)abstractOutput).getLongValue());
    }
}