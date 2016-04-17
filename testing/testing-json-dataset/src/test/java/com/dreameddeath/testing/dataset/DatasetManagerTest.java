package com.dreameddeath.testing.dataset;

import com.dreameddeath.testing.dataset.model.Dataset;
import com.dreameddeath.testing.dataset.model.DatasetElement;
import com.dreameddeath.testing.dataset.runtime.DatasetBuilder;
import com.dreameddeath.testing.dataset.runtime.DatasetResultValue;
import com.dreameddeath.testing.dataset.runtime.MvelRuntimeContext;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;

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

        DatasetBuilder runtime=new DatasetBuilder(dataset);
        runtime.run();
        DatasetResultValue resultingDataset = runtime.getDatasetResultByName("the first dataset");
        String resultAsJson=new ObjectMapper().writerWithDefaultPrettyPrinter().writeValueAsString(resultingDataset);
        assertTrue(resultAsJson.contains("\"a new test is borned\""));

    }
}