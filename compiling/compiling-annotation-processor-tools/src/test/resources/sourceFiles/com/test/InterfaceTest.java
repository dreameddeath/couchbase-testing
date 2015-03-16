package com.test;

import java.util.Map;
import com.dreameddeath.core.tools.annotation.processor.testing.TestingAnnotation;

@TestingAnnotation("test2")
public interface InterfaceTest{
    public String getterMethod();
    @AnnotationTest
    public String annotatedGetterMethod();

    public Map<String,InterfaceTest> complexRecuriveMethod();

    public void testingVoidResult();
}