package com.dreameddeath.core.storage;

import com.dreameddeath.core.annotation.DocumentDef;
import com.dreameddeath.core.annotation.DocumentProperty;
import com.dreameddeath.core.annotation.dao.DaoEntity;
import com.dreameddeath.core.annotation.processor.DaoAnnotationProcessor;
import com.dreameddeath.core.dao.business.BusinessCouchbaseDocumentDao;
import com.dreameddeath.core.model.business.BusinessCouchbaseDocument;
import com.dreameddeath.core.test.Utils;
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
public class DaoGenerator {

    @DocumentDef(domain="test",name="daoProccessor",version = "1.0.0")
    @DaoEntity(baseDao = BusinessCouchbaseDocumentDao.class,dbPath = "test",idFormat = "%010d",idPattern = "\\d{10}")
    public static class TestDao extends BusinessCouchbaseDocument {
        @DocumentProperty("value")
        public String value;
    }

    Utils.TestEnvironment _env;
    @Before
    public void initTest() throws  Exception{
        _env = new Utils.TestEnvironment("ViewTests");
       // _env.addDocumentDao(new TestDaoProcesorDao(),TestDaoProcessor.class);
        _env.start();
    }

    @Test
    public void testGeneratedDao(){

    }

    @Test
    public void runAnnotationProcessor() throws Exception {
        String source = this.getClass().getClassLoader().getResource("daoSourceFiles").getPath();;

        Iterable<JavaFileObject> files = getSourceFiles(source);

        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();


        JavaCompiler.CompilationTask task = compiler.getTask(new PrintWriter(System.out), null, null, null, null, files);
        task.setProcessors(Arrays.asList(new DaoAnnotationProcessor()));

        task.call();
    }

    private Iterable<JavaFileObject> getSourceFiles(String p_path) throws Exception {
        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        StandardJavaFileManager files = compiler.getStandardFileManager(null, null, null);

        files.setLocation(StandardLocation.SOURCE_PATH, Arrays.asList(new File(p_path)));

        Set<JavaFileObject.Kind> fileKinds = Collections.singleton(JavaFileObject.Kind.SOURCE);
        return files.list(StandardLocation.SOURCE_PATH, "", fileKinds, true);
    }
}
