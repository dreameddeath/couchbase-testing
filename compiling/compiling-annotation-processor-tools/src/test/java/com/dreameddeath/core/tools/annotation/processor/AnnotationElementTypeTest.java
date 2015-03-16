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