package com.dreameddeath.testing.dataset;

import com.dreameddeath.testing.dataset.model.Dataset;
import com.dreameddeath.testing.dataset.model.DatasetElement;
import com.dreameddeath.testing.dataset.runtime.MvelRuntimeContext;
import com.dreameddeath.testing.dataset.runtime.model.DatasetResultValue;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;

import java.util.Collections;

import static org.junit.Assert.*;

/**
 * Created by Christophe Jeunesse on 16/04/2016.
 */
public class DatasetManagerTest {

    @Test
    public void addDatasetsFromResourcePath() throws Exception {
        DatasetManager manager =new DatasetManager();
        manager.addDatasetsFromResourcePath("datasets/");
        manager.prepareDatasets();
        Dataset dataset =manager.getDatasetByName("test1");
        assertNotNull(dataset);
        DatasetElement element = dataset.getElements().get(2);

        MvelRuntimeContext context = new MvelRuntimeContext(dataset);
        Object result = context.execute(element.getMvel());
        assertEquals(3,result);
        Object result2 = context.execute(dataset.getElements().get(3).getMvel());
        assertEquals(6,result2);

        DatasetResultValue resultingDataset=manager.build("test1","the first dataset");
        String resultAsJson=new ObjectMapper().writerWithDefaultPrettyPrinter().writeValueAsString(resultingDataset);
        assertTrue(resultAsJson.contains("\"a new test is borned\""));

        DatasetResultValue resulting2Dataset=manager.build("test1","the second dataset");
        String result2AsJson=new ObjectMapper().writerWithDefaultPrettyPrinter().writeValueAsString(resulting2Dataset);
        assertTrue(result2AsJson.contains("\"new value\""));

        assertTrue(manager.validate(resultingDataset,"validationTest1","validateTest1", Collections.singletonMap("toto_value","tutut")));
        assertTrue(manager.validate(resulting2Dataset,"validationTest1","validateTest1", Collections.singletonMap("toto_value","new value")));
        assertTrue(manager.validate(resultingDataset,"validationTest1","validateTest1Success"));
        assertFalse(manager.validate(resultingDataset,"validationTest1","validateTest1Failure"));
        assertTrue(manager.validate(resultingDataset,"validationTest1","validateTest1SuccessWithStar"));
        assertTrue(manager.validate(resultingDataset,"validationTest1","validateTest1SuccessWithEmptyRule"));
        assertFalse(manager.validate(resultingDataset,"validationTest1","validateTest1FailureWithEmptyRule"));

    }
}