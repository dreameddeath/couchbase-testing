package com.dreameddeath.core.storage;


import com.dreameddeath.core.annotation.processor.DaoAnnotationProcessor;
import com.dreameddeath.core.test.Utils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import javax.tools.*;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
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
        StandardJavaFileManager manager = compiler.getStandardFileManager(null,null,null);
        Path tmpDir = Files.createTempDirectory(DaoGeneratorTest.class.getSimpleName());
        manager.setLocation(StandardLocation.CLASS_OUTPUT,
                Arrays.asList(tmpDir.toFile()));
        JavaCompiler.CompilationTask task = compiler.getTask(new PrintWriter(System.out), manager, null, null, null, files);
        task.setProcessors(Arrays.asList(new DaoAnnotationProcessor()));

        Boolean success = task.call();
        assertTrue(success);
        deleteRecursive(tmpDir.toFile());
    }

    private Iterable<JavaFileObject> getSourceFiles(String p_path) throws Exception {
        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        StandardJavaFileManager files = compiler.getStandardFileManager(null, null, null);

        files.setLocation(StandardLocation.SOURCE_PATH, Arrays.asList(new File(p_path)));

        Set<JavaFileObject.Kind> fileKinds = Collections.singleton(JavaFileObject.Kind.SOURCE);
        return files.list(StandardLocation.SOURCE_PATH, "", fileKinds, true);
    }

    public  boolean deleteRecursive(File path) throws FileNotFoundException {
        if (!path.exists()) throw new FileNotFoundException(path.getAbsolutePath());
        boolean ret = true;
        if (path.isDirectory()){
            for (File f : path.listFiles()){
                ret = ret && deleteRecursive(f);
            }
        }
        return ret && path.delete();
    }
}
