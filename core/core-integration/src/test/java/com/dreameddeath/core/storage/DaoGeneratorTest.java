package com.dreameddeath.core.storage;


import com.dreameddeath.core.annotation.processor.DaoAnnotationProcessor;
import com.dreameddeath.core.test.Utils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import javax.tools.*;
import java.io.File;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.Collections;
import java.util.Set;
/**
 * Created by CEAJ8230 on 02/01/2015.
 */
public class DaoGeneratorTest extends Assert {


    Utils.TestEnvironment _env;
    @Before
    public void initTest() throws  Exception{
        _env = new Utils.TestEnvironment("ViewTests");
       // _env.addDocumentDao(new TestDaoProcesorDao(),TestDaoProcessor.class);
        _env.start();
    }

    @Test
    public void runAnnotationProcessor() throws Exception {
        String source = this.getClass().getClassLoader().getResource("daoSourceFiles").getPath();

        Iterable<JavaFileObject> files = getSourceFiles(source);

        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();

        JavaCompiler.CompilationTask task = compiler.getTask(new PrintWriter(System.out), null, null, null, null, files);
        task.setProcessors(Arrays.asList(new DaoAnnotationProcessor()));

        Boolean success = task.call();
        assertTrue(success);

    }

    private Iterable<JavaFileObject> getSourceFiles(String p_path) throws Exception {
        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        StandardJavaFileManager files = compiler.getStandardFileManager(null, null, null);

        files.setLocation(StandardLocation.SOURCE_PATH, Arrays.asList(new File(p_path)));

        Set<JavaFileObject.Kind> fileKinds = Collections.singleton(JavaFileObject.Kind.SOURCE);
        return files.list(StandardLocation.SOURCE_PATH, "", fileKinds, true);
    }
}
