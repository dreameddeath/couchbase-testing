package com.test;

import com.dreameddeath.core.tools.annotation.processor.testing.GenericsTestExistingClass;
import com.dreameddeath.core.tools.annotation.processor.testing.TestingAnnotation;

import java.util.Map;
import java.io.Closeable;

@TestingAnnotation("test")
public interface GenericsInterfaceTest<TREQ,TVALUE extends Map<String,String> & Closeable> extends GenericsTestExistingClass<TREQ,TVALUE> {

}