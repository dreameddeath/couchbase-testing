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

package com.dreameddeath.core.tools.annotation.processor;

import com.dreameddeath.core.tools.annotation.processor.testing.TestingAnnotationProcessor;
import com.dreameddeath.testing.AnnotationProcessorTestingWrapper;
import org.junit.Assert;
import org.junit.Test;

public class AnnotationElementTypeTest extends Assert {
    @Test
    public void testAnnotationUtils() throws Exception{
        AnnotationProcessorTestingWrapper annotTester = new AnnotationProcessorTestingWrapper();
        annotTester.
                withAnnotationProcessor(new TestingAnnotationProcessor()).
                withTempDirectoryPrefix("AnnotationElementTypeTest");
        AnnotationProcessorTestingWrapper.Result result = annotTester.run(this.getClass().getClassLoader().getResource("sourceFiles").getPath());
        assertTrue(result.getResult());
        result.cleanUp();
    }
}