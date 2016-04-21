package com.dreameddeath.testing.dataset;

import com.dreameddeath.testing.dataset.converter.IDatasetResultConverter;
import com.dreameddeath.testing.dataset.json.grammar.JSON_DATASET;
import com.dreameddeath.testing.dataset.json.grammar.JSON_DATASET_LEXER;
import com.dreameddeath.testing.dataset.model.Dataset;
import com.dreameddeath.testing.dataset.runtime.builder.DatasetBuilder;
import com.dreameddeath.testing.dataset.runtime.model.DatasetResult;
import com.dreameddeath.testing.dataset.runtime.model.DatasetResultValue;
import com.dreameddeath.testing.dataset.runtime.validator.DatasetValidator;
import com.google.common.base.Preconditions;
import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

import java.io.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by Christophe Jeunesse on 14/04/2016.
 */
public class DatasetManager {
    private Map<String,Dataset> datasetMap=new ConcurrentHashMap<>();

    private final ServiceLoader<IDatasetResultConverter> mapperServiceLoader=ServiceLoader.load(IDatasetResultConverter.class);
    private final List<IDatasetResultConverter> mappers=new ArrayList<>();
    private final Map<Class<?>,IDatasetResultConverter> mapperPerClass=new HashMap<>();
    private boolean isPrepared=false;

    public void initMapper(){
        mappers.clear();
        mapperServiceLoader.reload();
        Iterator<IDatasetResultConverter> mapperIterator=mapperServiceLoader.iterator();
        while(mapperIterator.hasNext()){
            mappers.add(mapperIterator.next());
        }
    }

    public void checkPrepared(){
        Preconditions.checkArgument(isPrepared,"The prepared hasn't been called");
    }

    public DatasetManager(){
        initMapper();
    }

    public <T> IDatasetResultConverter<T> getMapperForClass(Class<T> clazz){
        return mapperPerClass.computeIfAbsent(clazz,cls->{
            for(IDatasetResultConverter<?> mapper:mappers){
                if(mapper.canMap(clazz)){
                    return mapper;
                }
            }
            throw new IllegalArgumentException("Class mapper "+clazz.getName()+" not found");
        });
    }


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
        addDatasetsFromResourceFilename(resourceName + "/*");
    }

    public void addDatasetsFromResourceFilename(String resourceName){
        PathMatchingResourcePatternResolver resourcePatternResolver = new PathMatchingResourcePatternResolver(Thread.currentThread().getContextClassLoader());
        try {
            Resource[] resources = resourcePatternResolver.getResources("classpath:" + resourceName);
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
        isPrepared=true;
    }

    public Dataset getDatasetByName(String name){
        return datasetMap.get(name);
    }



    private <T> T internalBuilder(String datasetName, String datasetElementName, Map<String,Object> params, Class<T> tClass){
        checkPrepared();
        Dataset dataset=getDatasetByName(datasetName);
        Preconditions.checkNotNull(datasetName);
        if(DatasetResultValue.class.isAssignableFrom(tClass)) {
            return (T) new DatasetBuilder(dataset, params).build(datasetElementName);
        }
        else{
            return new DatasetBuilder(dataset, params).build(tClass,datasetElementName);
        }
    }

    public DatasetResultValue build(String datasetName,String datasetElementName,Map<String,Object> params){
        return internalBuilder(datasetName,datasetElementName,params,DatasetResultValue.class);
    }

    public DatasetResultValue build(String datasetName,String datasetElementName){
        return build(datasetName,datasetElementName,Collections.emptyMap());
    }

    public <T> T build(Class<T> clazz,String datasetName,String datasetElementName,Map<String,Object> params){
        return internalBuilder(datasetName,datasetElementName,params,clazz);
    }

    public <T> T build(Class<T> clazz,String datasetName,String datasetElementName){
        return build(clazz,datasetName,datasetElementName,Collections.emptyMap());
    }

    public DatasetResult build(String datasetName){
        return build(datasetName,Collections.emptyMap());
    }

    public DatasetResult build(String datasetName,Map<String,Object> params){
        checkPrepared();
        Dataset dataset=getDatasetByName(datasetName);
        Preconditions.checkNotNull(datasetName);
        return new DatasetBuilder(dataset, params).run();
    }



    public boolean validate(DatasetResultValue value,String datasetName,String dataSetElementName,Map<String,Object> params){
        checkPrepared();
        Dataset dataset=getDatasetByName(datasetName);
        Preconditions.checkNotNull(dataset);
        return new DatasetValidator(dataset,params).validate(value,dataSetElementName);
    }

    public boolean validate(DatasetResultValue value,String datasetName,String dataSetElementName){
        return validate(value,datasetName,dataSetElementName, Collections.emptyMap());
    }

    public <T> boolean validate(T value,String datasetName,String dataSetElementName,Map<String,Object> params){
        if(value instanceof DatasetResultValue){
            return validate((DatasetResultValue)value,datasetName,dataSetElementName, params);
        }
        else{
            IDatasetResultConverter<T> mapper=getMapperForClass((Class<T>)value.getClass());
            if(mapper!=null){
                return validate(mapper.mapObject(value),datasetName,dataSetElementName,params);
            }
            else{
                throw new RuntimeException("Cannot convert class <"+value.getClass().getName()+">");
            }
        }
    }

    public <T> boolean validate(T value,String datasetName,String dataSetElementName){
        return validate(value,datasetName,dataSetElementName,Collections.emptyMap());
    }

}
