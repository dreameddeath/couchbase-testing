package com.dreameddeath.testing.dataset.json.grammar;

import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.junit.Test;

import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;

public class JsonGrammarTest {

    @Test
    public void simpleGrammarText(){
        String simpleJson="{ toto[0].utut[1..-1].\"tata\": \"the given value\"}";

        JSON_DATASET_LEXER lexer = new JSON_DATASET_LEXER(new ANTLRInputStream(simpleJson));
        JSON_DATASET parser = new JSON_DATASET(new CommonTokenStream(lexer));

        JSON_DATASET.JsonContext json = parser.json();
        System.out.println(json.result);
        assertTrue(json.result instanceof Map);
        assertEquals(1, ((Map) json.result).size());
        for(Map.Entry element:((Map<Object,Object>) json.result).entrySet()){
            assertTrue(element.getKey() instanceof List);
            assertEquals(3, ((List)element.getKey()).size());
            assertArrayEquals(new String[]{"toto[0]","utut[1..-1]","\"tata\""}, ((List<String>)element.getKey()).toArray());
            assertTrue(element.getValue() instanceof String);
            assertEquals("\"the given value\"", element.getValue().toString());
        }
        //assertEquals(1,((Map)json.result).keySet().iterator().next());
    }

}