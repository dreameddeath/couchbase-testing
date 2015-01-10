package com.dreameddeath.core.storage;


import com.dreameddeath.core.annotation.processor.DaoAnnotationProcessor;
import com.dreameddeath.core.test.Utils;
import com.dreameddeath.testing.AnnotationProcessorTestingWrapper;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
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
        AnnotationProcessorTestingWrapper annotTester = new AnnotationProcessorTestingWrapper();
        annotTester.
                withAnnotationProcessor(new DaoAnnotationProcessor()).
                withTempDirectoryPrefix("DaoAnnotationProcessorTest");
        AnnotationProcessorTestingWrapper.Result result = annotTester.run(this.getClass().getClassLoader().getResource("daoSourceFiles").getPath());
        assertTrue(result.getResult());
        assertTrue(result.hasClass("test.dao.TestDaoDao"));
        result.cleanUp();
    }
}
