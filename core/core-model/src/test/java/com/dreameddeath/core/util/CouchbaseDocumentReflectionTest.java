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

package com.dreameddeath.core.util;

import com.dreameddeath.core.model.annotation.DocumentProperty;
import com.dreameddeath.core.model.document.CouchbaseDocument;
import com.dreameddeath.core.model.document.CouchbaseDocumentElement;
import com.dreameddeath.core.model.property.Property;
import com.dreameddeath.core.model.property.impl.StandardProperty;
import com.dreameddeath.core.model.util.CouchbaseDocumentReflection;
import com.dreameddeath.testing.AnnotationProcessorTestingWrapper;
import org.junit.Test;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Messager;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class CouchbaseDocumentReflectionTest {


    public static class TestElement extends CouchbaseDocumentElement{
        @DocumentProperty("test")
        public String _test;
    }

    @TestAnnotation
    public static class TestClass extends CouchbaseDocument{
        @DocumentProperty("testSimple")
        public String _testSimple;

        @DocumentProperty("testCollection")
        public List<String> _testCollection;

        @DocumentProperty("testMap")
        public Map<String,String> _testMap;

        @DocumentProperty("testElement")
        public TestElement _element;

        @DocumentProperty("testComplexElement")
        public Set<? extends TestElement> _complexElement;

    }

    public static class TestInherited extends TestClass{
        /**
         *  docProp : propertyTest
         */
        @DocumentProperty("docProp")
        private Property<Long> _docProp = new StandardProperty<Long>(TestInherited.this);

        // docProp accessors
        public Long getDocProp() { return _docProp.get(); }
        public void setDocProp(Long val) { _docProp.set(val); }

        /**
         *  modifiedAccessors : custom getter/setter
         */
        @DocumentProperty(value = "modifiedAccessors",getter = "getModifiedAccessors2",setter = "setModifiedAccessors2")
        private Property<String> _modifiedAccessors = new StandardProperty<String>(TestInherited.this);

        // modifiedAccessors accessors
        public String getModifiedAccessors2() { return _modifiedAccessors.get(); }
        public void setModifiedAccessors2(String val) { _modifiedAccessors.set(val); }
    }



    @Test
    public void testCouchbaseDocumentReflection() throws Exception{
        CouchbaseDocumentReflection refectionResult = CouchbaseDocumentReflection.getReflectionFromClass(TestInherited.class);

        assertEquals(2, refectionResult.getStructure().getDeclaredFields().size());
        assertEquals(7, refectionResult.getStructure().getFields().size());
        assertEquals(5,refectionResult.getSuperclassReflection().getStructure().getDeclaredFields().size());

        assertEquals(TestElement.class, refectionResult.getStructure().getFieldByPropertyName("testComplexElement").getCollectionElementClass());
        assertEquals(TestInherited.class.getDeclaredMethod("getDocProp"),refectionResult.getStructure().getFieldByPropertyName("docProp").getGetter().getMember());
        assertEquals(TestInherited.class.getDeclaredMethod("setDocProp", Long.class), refectionResult.getStructure().getFieldByPropertyName("docProp").getSetter().getMember());
        assertEquals(TestInherited.class.getDeclaredMethod("getModifiedAccessors2"),refectionResult.getStructure().getFieldByPropertyName("modifiedAccessors").getGetter().getMember());
        assertEquals(TestInherited.class.getDeclaredMethod("setModifiedAccessors2", String.class), refectionResult.getStructure().getFieldByPropertyName("modifiedAccessors").getSetter().getMember());


        AnnotationProcessorTestingWrapper wrapper = new AnnotationProcessorTestingWrapper();
        wrapper
                .withTempDirectoryPrefix("CouchbaseDocumentReflectionTest")
                .withAnnotationProcessor(new TestAnnotationProcessor());

        Map<String,String> sources = new HashMap<>();

        StringWriter writer = new StringWriter();
        PrintWriter out = new PrintWriter(writer);
        out.println("package com.dreameddeath.core.util;");
        out.println("import java.util.Collection;");
        out.println("import com.dreameddeath.core.util.CouchbaseDocumentReflectionTest.TestInherited;");
        out.println("import com.dreameddeath.core.model.document.CouchbaseDocumentElement;");
        out.println("import com.dreameddeath.core.util.*;");
        out.println("import com.dreameddeath.core.model.annotation.DocumentProperty;");
        out.println("@TestAnnotation");
        out.println("public class TypeMirrorInherited extends TestInherited {");
        out.println("  @DocumentProperty(\"subValue1\") private String subValue1;");
        out.println("  public String getSubValue1(){ return subValue1;}");
        out.println("  public void setSubValue1(String value){ subValue1=value;}");
        out.println("  @DocumentProperty(\"subValue2\") public Collection<String> subValue2;");
        out.println("  @DocumentProperty(\"subValue3\") public SubTypeMirrorInherited subValue3;");
        out.println("  @DocumentProperty(\"subValue4\") public Collection<? extends SubTypeMirrorInherited> subValue4;");
        out.println("  @TestAnnotation public static class SubTypeMirrorInherited extends TypeMirrorInherited {");
        out.println("  }");
        out.println("}");
        out.close();
        sources.put("com.dreameddeath.core.util.TypeMirrorInherited", writer.toString());

        AnnotationProcessorTestingWrapper.Result compilingResult = wrapper.run(sources);
        assertTrue(compilingResult.getResult());
        Class compiledClass = compilingResult.getClass("com.dreameddeath.core.util.TypeMirrorInherited");
        assertEquals("TypeMirrorInherited",compiledClass.getSimpleName());
        compilingResult.cleanUp();
    }



    @SupportedAnnotationTypes(
            {"com.dreameddeath.core.util.TestAnnotation"}
    )
    public static class TestAnnotationProcessor extends AbstractProcessor {

        @Override
        public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
            Messager messager = processingEnv.getMessager();
            for (Element classElem : roundEnv.getElementsAnnotatedWith(TestAnnotation.class)) {
                CouchbaseDocumentReflection result = CouchbaseDocumentReflection.getReflectionFromTypeElement((TypeElement)classElem);
                assertEquals(classElem.getSimpleName().toString(),result.getSimpleName());
            }
            return false;
        }

        @Override
        public SourceVersion getSupportedSourceVersion() {
            return SourceVersion.latestSupported();
        }

    }
}