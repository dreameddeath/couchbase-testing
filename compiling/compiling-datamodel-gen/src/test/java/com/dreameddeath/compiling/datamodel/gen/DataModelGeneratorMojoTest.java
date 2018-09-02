package com.dreameddeath.compiling.datamodel.gen;

import com.dreameddeath.testing.AnnotationProcessorTestingWrapper;
import org.apache.maven.plugin.testing.AbstractMojoTestCase;

import java.io.File;

public class DataModelGeneratorMojoTest  extends AbstractMojoTestCase {

    /**
     * @throws Exception if any
     */
    public void testSomething()
            throws Exception
    {
        File pom = getTestFile( "src/test/resources/maven/pom.xml" );
        assertNotNull( pom );
        assertTrue( pom.exists() );

        DataModelGeneratorMojo myMojo = (DataModelGeneratorMojo) lookupMojo( "compiling-datamodel-gen", pom );
        assertNotNull( myMojo );
        myMojo.execute();

        AnnotationProcessorTestingWrapper annotationProcessorTestingWrapper = new AnnotationProcessorTestingWrapper();
        AnnotationProcessorTestingWrapper.Result result = annotationProcessorTestingWrapper.run(myMojo.outputPath.getAbsolutePath());
        assertTrue(result.getResult());
    }

}