/*
 * Copyright Christophe Jeunesse
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */

package com.dreameddeath.core.model.mapper.impl;

import com.dreameddeath.core.model.annotation.DocumentEntity;
import com.dreameddeath.core.model.document.CouchbaseDocument;
import com.dreameddeath.core.model.exception.mapper.DuplicateMappedEntryInfoException;
import com.dreameddeath.core.model.exception.mapper.MappingNotFoundException;
import com.dreameddeath.core.model.mapper.IDocumentClassMappingInfo;
import com.dreameddeath.core.model.mapper.IKeyMappingInfo;
import org.junit.Test;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import static org.junit.Assert.*;

/**
 * Created by Christophe Jeunesse on 08/06/2015.
 */
public class DefaultDocumentMapperInfoTest {
    public static class TestRootDoc extends CouchbaseDocument {
    }

    public static class TestChildDoc extends TestRootDoc {
    }

    public static class TestGrandChildDoc extends TestChildDoc {
    }

    public static class TestIndepDoc extends CouchbaseDocument {
    }

    public static class TestNotFoundDoc extends CouchbaseDocument {
    }


    @Test
    public void test() throws Exception {
        DefaultDocumentMapperInfo mapperInfo = new DefaultDocumentMapperInfo();
        mapperInfo.addDocument(TestRootDoc.class, "toto/\\d+");
        mapperInfo.addDocument(TestIndepDoc.class, "indep");

        IDocumentClassMappingInfo perClassMappingInfo = mapperInfo.getMappingFromClass(TestRootDoc.class);
        IKeyMappingInfo keyMappingInfo = mapperInfo.getFirstMappingFromKey("toto/10");
        assertEquals("toto/10", keyMappingInfo.fullKey());
        assertEquals("toto/10", keyMappingInfo.key());
        assertNull(keyMappingInfo.prefix());
        assertEquals(perClassMappingInfo, keyMappingInfo.classMappingInfo());
        assertEquals(0, perClassMappingInfo.getChildClasses().size());

        IKeyMappingInfo infoPrefixClass = mapperInfo.getFirstMappingFromKey("prefix$toto/10");
        assertEquals("prefix$toto/10", infoPrefixClass.fullKey());
        assertEquals("toto/10", infoPrefixClass.key());
        assertEquals("prefix", infoPrefixClass.prefix());
        assertEquals(perClassMappingInfo, infoPrefixClass.classMappingInfo());

        IDocumentClassMappingInfo grandChildMappingInfo = mapperInfo.getMappingFromClass(TestGrandChildDoc.class);
        assertEquals(TestRootDoc.class, grandChildMappingInfo.classRootInfo());
        assertEquals(2, perClassMappingInfo.getChildClasses().size());

        perClassMappingInfo.attachObject(String.class, "testing");

        assertEquals("testing", grandChildMappingInfo.getAttachedObject(String.class));

        try {
            mapperInfo.addDocument(TestRootDoc.class, "toto/\\d+");
            fail("Should have generated a duplicate exception");
        } catch (DuplicateMappedEntryInfoException e) {
            //ignore error
        }

        try {
            mapperInfo.getFirstMappingFromKey("toto/1Osuffix");
            fail("Should have generated a not found exception");
        } catch (MappingNotFoundException e) {
            //ignore error
        }

        try {
            mapperInfo.getMappingFromClass(TestNotFoundDoc.class);
            fail("Should have generated a not found exception");
        } catch (MappingNotFoundException e) {
            //ignore error
        }
    }

    @DocumentEntity(domain = "toto", version = "1.0.0")
    public abstract static class TestRootDocForDomain extends CouchbaseDocument {
    }

    @DocumentEntity(domain = "testChild", version = "1.0.0")
    public static class TestChildDocForDomain extends TestRootDocForDomain {
    }

    @DocumentEntity(domain = "testChild", version = "1.0.0")
    public static class TestGrandChildDocForDomain extends TestChildDocForDomain {
    }

    @DocumentEntity(domain = "testSibling", version = "1.0.0")
    public static class TestSiblingForDomain extends TestRootDocForDomain {
    }

    @DocumentEntity(domain = "testSibling", version = "1.0.0")
    public static class TestGrandChildSiblingForDomain extends TestSiblingForDomain {
    }

    @Test
    public void testWithDomains() throws Exception {
        DefaultDocumentMapperInfo mapperInfo = new DefaultDocumentMapperInfo();
        mapperInfo.addDocument(TestRootDocForDomain.class, "toto/\\d+", "testChild");
        mapperInfo.addDocument(TestRootDocForDomain.class, "toto/\\d+", "testSibling");

        try {
            IDocumentClassMappingInfo perClassMappingInfo = mapperInfo.getMappingFromClass(TestRootDocForDomain.class);
            fail("Shouldn't be found");
        } catch (MappingNotFoundException e) {

        }
        try {
            IDocumentClassMappingInfo perClassMappingInfo = mapperInfo.getMappingFromClass(TestRootDocForDomain.class);
            fail("Shouldn't be found");
        } catch (MappingNotFoundException e) {

        }
        Set<String> domains = new HashSet<>(Arrays.asList("testChild", "testSibling"));
        for (String domain : domains) {
            IDocumentClassMappingInfo perClassMappingInfo = mapperInfo.getMappingFromClass(domain, TestRootDocForDomain.class);
            IKeyMappingInfo keyMappingInfo = mapperInfo.getMappingFromKey( domain,"toto/10");
            assertEquals("toto/10", keyMappingInfo.fullKey());
            assertEquals("toto/10", keyMappingInfo.key());
            assertNull(keyMappingInfo.prefix());
            assertEquals(perClassMappingInfo, keyMappingInfo.classMappingInfo());
            assertEquals(0, perClassMappingInfo.getChildClasses().size());

            IKeyMappingInfo infoPrefixClass = mapperInfo.getMappingFromKey(domain,"prefix$toto/10");
            assertEquals("prefix$toto/10", infoPrefixClass.fullKey());
            assertEquals("toto/10", infoPrefixClass.key());
            assertEquals("prefix", infoPrefixClass.prefix());
            assertEquals(perClassMappingInfo, infoPrefixClass.classMappingInfo());

            IDocumentClassMappingInfo grandChildMappingInfo = mapperInfo.getMappingFromClass(domain, TestGrandChildDocForDomain.class);
            assertEquals(TestRootDocForDomain.class, grandChildMappingInfo.classRootInfo());
            assertEquals(2, perClassMappingInfo.getChildClasses().size());

            perClassMappingInfo.attachObject(String.class, "testing");

            assertEquals("testing", grandChildMappingInfo.getAttachedObject(String.class));
            try {
                mapperInfo.addDocument(TestRootDocForDomain.class, "toto/\\d+", domain);
                fail("Should have generated a duplicate exception");
            } catch (DuplicateMappedEntryInfoException e) {
                //ignore error
            }

            try {
                mapperInfo.getMappingFromKey(domain, "toto/1Osuffix");
                fail("Should have generated a not found exception");
            } catch (MappingNotFoundException e) {
                //ignore error
            }

            try {
                mapperInfo.getMappingFromClass(domain, TestNotFoundDoc.class);
                fail("Should have generated a not found exception");
            } catch (MappingNotFoundException e) {
                //ignore error
            }
        }
        IDocumentClassMappingInfo grandChildMappingInfo = mapperInfo.getMappingFromClass(TestGrandChildDocForDomain.class);
        assertEquals(TestRootDocForDomain.class, grandChildMappingInfo.classRootInfo());
        IDocumentClassMappingInfo childMappingSiblingInfo = mapperInfo.getMappingFromClass(TestSiblingForDomain.class);
        assertEquals(TestRootDocForDomain.class, childMappingSiblingInfo.classRootInfo());

        //assertEquals(2, perClassMappingInfo.getChildClasses().size());
        IDocumentClassMappingInfo grandChildMappingSiblingInfo = mapperInfo.getMappingFromClass(TestGrandChildSiblingForDomain.class);
        assertEquals(TestRootDocForDomain.class, grandChildMappingSiblingInfo.classRootInfo());

    }
}