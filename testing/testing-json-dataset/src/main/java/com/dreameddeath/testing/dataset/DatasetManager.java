package com.dreameddeath.testing.dataset;

import com.dreameddeath.testing.dataset.json.grammar.JSON_DATASET;
import com.dreameddeath.testing.dataset.json.grammar.JSON_DATASET_LEXER;
import com.dreameddeath.testing.dataset.model.Dataset;
import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Christophe Jeunesse on 14/04/2016.
 */
public class DatasetManager {
    private Map<String,Dataset> datasetMap=new HashMap<>();






    public Dataset newDataset(ANTLRInputStream inputStream){
        JSON_DATASET_LEXER lexer = new JSON_DATASET_LEXER(inputStream);
        JSON_DATASET parser = new JSON_DATASET(new CommonTokenStream(lexer));
        Dataset result = parser.dataset().result;
        if(result.getName()!=null){
            datasetMap.put(result.getName(),result);
        }
        else if(inputStream.name!=null){
            datasetMap.put(inputStream.name,result);
        }
        else{
            throw new RuntimeException("Dataset must have a name");
        }
        result.setManager(this);
        return result;
    }

    public Dataset newDataset(InputStream inputStream,String name){
        try {
            ANTLRInputStream antlrInputStream = new ANTLRInputStream(inputStream);
            antlrInputStream.name = name;
            return this.newDataset(antlrInputStream);
        }
        catch(IOException e){
            throw new RuntimeException(e);
        }
    }

    public Dataset newDatasetFromFile(File filename){
        try {
            return this.newDataset(new FileInputStream(filename),filename.getName());
        }
        catch(FileNotFoundException e){
            throw new RuntimeException(e);
        }
    }

    public void addDatasetsFromResourcePath(String resourceName){
        PathMatchingResourcePatternResolver resourcePatternResolver = new PathMatchingResourcePatternResolver(Thread.currentThread().getContextClassLoader());
        try {
            Resource[] resources = resourcePatternResolver.getResources("classpath:" + resourceName + "/*");
            for(Resource resource:resources){
                newDatasetFromFile(resource.getFile());
            }
        }
        catch(IOException e){
            throw new RuntimeException(e);
        }

    }
}
