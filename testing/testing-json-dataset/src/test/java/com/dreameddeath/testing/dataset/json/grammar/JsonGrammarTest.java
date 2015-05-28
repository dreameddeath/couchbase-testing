/*
 * Copyright Christophe Jeunesse
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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