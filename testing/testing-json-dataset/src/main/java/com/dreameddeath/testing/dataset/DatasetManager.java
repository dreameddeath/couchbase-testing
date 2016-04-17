package com.dreameddeath.testing.dataset;

import com.dreameddeath.testing.dataset.json.grammar.JSON_DATASET;
import com.dreameddeath.testing.dataset.json.grammar.JSON_DATASET_LEXER;
import com.dreameddeath.testing.dataset.model.Dataset;
import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

import java.io.*;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by Christophe Jeunesse on 14/04/2016.
 */
public class DatasetManager {
    private Map<String,Dataset> datasetMap=new ConcurrentHashMap<>();

    synchronized public void registerDataset(String name,Dataset dataset){
        if(datasetMap.containsKey(name)){
            throw new RuntimeException("The dataset <"+name+"> is already existing");
        }
        datasetMap.put(name,dataset);
    }

    public Dataset newDataset(ANTLRInputStream inputStream){
        JSON_DATASET_LEXER lexer = new JSON_DATASET_LEXER(inputStream);
        JSON_DATASET parser = new JSON_DATASET(new CommonTokenStream(lexer));
        Dataset result = parser.dataset().result;
        if(result.getName()!=null){
            registerDataset(result.getName(),result);
        }
        else if(inputStream.name!=null){
            result.setName(inputStream.name);
            registerDataset(result.getName(),result);
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
            String name = filename.getName();
            if(name.lastIndexOf(".")>0){
                name=name.substring(0,name.lastIndexOf("."));
            }
            return this.newDataset(new FileInputStream(filename),name);
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

    public void prepareDatasets(){
        for(Dataset dataset:datasetMap.values()){
            dataset.prepare();
        }
    }

    public Dataset getDatasetByName(String name){
        return datasetMap.get(name);
    }

}
