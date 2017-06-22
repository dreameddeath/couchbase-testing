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

package com.dreameddeath.core.model.dto.annotation.processor;

import com.dreameddeath.core.model.dto.annotation.processor.converter.ConverterDefAnnotationProcessor;
import com.dreameddeath.core.model.dto.annotation.processor.converter.ConverterGeneratorProcessor;
import com.dreameddeath.core.model.dto.annotation.processor.model.StandardCouchbaseDocumentDtoModelGeneratorProcessor;
import com.dreameddeath.testing.AnnotationProcessorTestingWrapper;
import org.junit.Test;

import static org.junit.Assert.assertTrue;

/**
 * Created by ceaj8230 on 04/02/2017.
 */
public class DtoGeneratorProcessorTest {

    @Test
    public void testGenerator() throws Exception{
        AnnotationProcessorTestingWrapper annotTester = new AnnotationProcessorTestingWrapper();
        annotTester.
                withAnnotationProcessor(new StandardCouchbaseDocumentDtoModelGeneratorProcessor()).
                withAnnotationProcessor(new ConverterGeneratorProcessor()).
                withAnnotationProcessor(new ConverterDefAnnotationProcessor()).
                withTempDirectoryPrefix("queryGenSourceFiles");
        AnnotationProcessorTestingWrapper.Result generatorResult = annotTester.run(DtoGeneratorProcessorTest.class.getClassLoader().getResource("queryGenSourceFiles").getPath());
        assertTrue(generatorResult.getResult());
    }

}