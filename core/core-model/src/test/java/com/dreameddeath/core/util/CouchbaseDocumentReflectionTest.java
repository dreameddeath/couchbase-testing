package com.dreameddeath.core.util;

import com.dreameddeath.core.annotation.DocumentProperty;
import com.dreameddeath.core.model.document.CouchbaseDocument;
import com.dreameddeath.core.model.document.CouchbaseDocumentElement;
import com.dreameddeath.core.model.property.Property;
import com.dreameddeath.core.model.property.impl.StandardProperty;
import org.junit.Test;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Messager;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.tools.*;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URI;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.Assert.assertEquals;

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
        System.out.println(TestAnnotation.class.getName());
        CouchbaseDocumentReflection result = CouchbaseDocumentReflection.getReflectionFromClass(TestInherited.class);

        assertEquals(2,result.getStructure().getDeclaredFields().size());
        assertEquals(7,result.getStructure().getFields().size());
        assertEquals(5,result.getSuperclassReflection().getStructure().getDeclaredFields().size());

        assertEquals(TestElement.class,result.getStructure().getFieldByName("testComplexElement").getCollectionElementClass());
        assertEquals(TestInherited.class.getDeclaredMethod("getDocProp"),result.getStructure().getFieldByName("docProp").getGetter());
        assertEquals(TestInherited.class.getDeclaredMethod("setDocProp",Long.class),result.getStructure().getFieldByName("docProp").getSetter());
        assertEquals(TestInherited.class.getDeclaredMethod("getModifiedAccessors2"),result.getStructure().getFieldByName("modifiedAccessors").getGetter());
        assertEquals(TestInherited.class.getDeclaredMethod("setModifiedAccessors2",String.class),result.getStructure().getFieldByName("modifiedAccessors").getSetter());


        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();

        StringWriter writer = new StringWriter();
        PrintWriter out = new PrintWriter(writer);
        out.println("package com.dreameddeath.core.util;");
        out.println("import java.util.Collection;");
        out.println("import com.dreameddeath.core.util.CouchbaseDocumentReflectionTest.TestInherited;");
        out.println("import com.dreameddeath.core.model.document.CouchbaseDocumentElement;");
        out.println("import com.dreameddeath.core.util.*;");
        out.println("import com.dreameddeath.core.annotation.DocumentProperty;");
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
        JavaFileObject file = new JavaSourceFromString("com.dreameddeath.core.util.TypeMirrorInherited", writer.toString());

        Iterable<? extends JavaFileObject> compilationUnits = Arrays.asList(file);

        DiagnosticCollector<JavaFileObject> diagnostics = new DiagnosticCollector<JavaFileObject>();

        JavaCompiler.CompilationTask task = compiler.getTask(null, null, diagnostics, null, null, compilationUnits);
        task.setProcessors(Arrays.asList(new TestAnnotationProcessor()));

        Boolean success = task.call();
        System.out.println("Success: " + success);


        /*Class compiledClass = Class.forName("com.dreameddeath.core.util.TypeMirrorInherited");
        assertEquals("TypeMirrorInherited",compiledClass.getSimpleName());*/
    }

    class JavaSourceFromString extends SimpleJavaFileObject {
        final String code;

        JavaSourceFromString(String name, String code) {
            super(URI.create("string:///" + name.replace('.', '/') + Kind.SOURCE.extension),Kind.SOURCE);
            this.code = code;
        }

        @Override
        public CharSequence getCharContent(boolean ignoreEncodingErrors) {
            return code;
        }
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