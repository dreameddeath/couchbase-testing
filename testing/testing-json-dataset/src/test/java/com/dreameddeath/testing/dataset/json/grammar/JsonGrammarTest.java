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

import com.dreameddeath.testing.dataset.model.*;
import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class JsonGrammarTest {

    @Test
    public void simpleGrammarText(){
        String simpleJson="{ @Testing\ntoto[0].utut[1..-1].\"ta\\\"ta\": \"the given value\"}";

        JSON_DATASET_LEXER lexer = new JSON_DATASET_LEXER(new ANTLRInputStream(simpleJson));
        JSON_DATASET parser = new JSON_DATASET(new CommonTokenStream(lexer));

        JSON_DATASET.DatasetContext datasetContext = parser.dataset();
        Dataset dataset = datasetContext.result;
        assertEquals(1, dataset.getElements().size());
        DatasetElement element = dataset.getElements().get(0);
        assertEquals(DatasetElement.Type.OBJECT, element.getType());
        for(DatasetObjectNode objectNode:dataset.getElements().get(0).getObject().getNodes()){
            DatasetXPath path = objectNode.getXPath();
            assertEquals(1, path.getMetas().size());
            assertEquals("Testing",path.getMetas().get(0).getName());
            assertEquals(3, path.getParts().size());
            assertEquals("toto[0]",path.getParts().get(0).getPath());
            assertEquals("utut[1..-1]",path.getParts().get(1).getPath());
            assertEquals("ta\"ta",path.getParts().get(2).getPath());
            assertEquals(DatasetValue.Type.STRING, objectNode.getValue().getType());
            assertEquals("the given value", objectNode.getValue().getContent());
        }
        //assertEquals(1,((Map)json.result).keySet().iterator().next());
    }

}