package com.dreameddeath.testing.dataset;

import org.junit.Test;

/**
 * Created by Christophe Jeunesse on 16/04/2016.
 */
public class DatasetManagerTest {

    @Test
    public void addDatasetsFromResourcePath() throws Exception {
        DatasetManager manager =new DatasetManager();
        manager.addDatasetsFromResourcePath("datasets/");
    }
}