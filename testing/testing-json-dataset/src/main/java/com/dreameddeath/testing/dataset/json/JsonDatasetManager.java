package com.dreameddeath.testing.dataset.json;

import com.dreameddeath.testing.dataset.json.grammar.JSON_DATASET;
import com.dreameddeath.testing.dataset.json.grammar.JSON_DATASET_LEXER;
import com.dreameddeath.testing.dataset.model.Dataset;
import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CommonTokenStream;

import java.io.*;

/**
 * Created by Christophe Jeunesse on 14/04/2016.
 */
public class JsonDatasetManager {


    public Dataset newDataset(ANTLRInputStream inputStream){
        JSON_DATASET_LEXER lexer = new JSON_DATASET_LEXER(inputStream);
        JSON_DATASET parser = new JSON_DATASET(new CommonTokenStream(lexer));
        return parser.dataset().result;
    }

    public Dataset newDataset(InputStream inputStream){
        try {
            return this.newDataset(new ANTLRInputStream(inputStream));
        }
        catch(IOException e){
            throw new RuntimeException(e);
        }
    }

    public Dataset newDatasetFromFile(File filename){
        try {
            return this.newDataset(new FileInputStream(filename));
        }
        catch(FileNotFoundException e){
            throw new RuntimeException(e);
        }
    }
}
