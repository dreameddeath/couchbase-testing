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

package com.dreameddeath.core.model.entity;

import com.dreameddeath.core.model.annotation.DocumentEntity;
import com.dreameddeath.core.model.document.CouchbaseDocument;
import com.dreameddeath.core.model.entity.model.EntityDef;
import com.dreameddeath.core.model.entity.model.EntityModelId;
import com.dreameddeath.core.model.util.CouchbaseDocumentStructureReflection;
import org.junit.Test;

import java.util.List;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Created by CEAJ8230 on 23/01/2017.
 */
public class EntityDefinitionManagerTest {
    @DocumentEntity(domain = "test",version = "1.0")
    public class StandardTestDoc extends CouchbaseDocument{}
    @DocumentEntity(domain = "testChild",version = "1.0")
    public class StandardChildTestDocSameDomain extends StandardTestDoc{}
    @DocumentEntity(domain = "testDomainDifferent",version = "2.0")
    public class StandardChildTestDocDifferentDomain extends StandardTestDoc{}
    @DocumentEntity(domain = "testDomainAbstract",version = "1.0")
    public abstract class StandardChildTestDocAbstractDifferentDomain extends StandardTestDoc{}
    @DocumentEntity(domain = "testDomainChildAbstract",version = "1.0")
    public class StandardGrandChildTestDocAbstractDifferentDomain extends StandardChildTestDocAbstractDifferentDomain{}
    @DocumentEntity(domain = "testDomainChildSiblingAbstract",version = "1.0")
    public class StandardGrandChildTestDocSiblingAbstractDifferentDomain extends StandardChildTestDocAbstractDifferentDomain{}

    @Test
    public void entityManagerTest(){
        final EntityDefinitionManager manager = new EntityDefinitionManager();

        List<EntityDef> entities = manager.getEntities().stream()
                .filter(entityDef -> manager.findClassFromVersionnedTypeId(entityDef.getModelId()).getPackage().equals(EntityDefinitionManagerTest.class.getPackage()))
                .collect(Collectors.toList());

        assertThat(entities)
                .hasSize(6)
                .extracting("modelId")
                .containsExactlyInAnyOrder(
                    EntityModelId.build("test","standardtestdoc","1.0"),
                    EntityModelId.build("testChild","standardchildtestdocsamedomain","1.0"),
                    EntityModelId.build("testDomainDifferent","standardchildtestdocdifferentdomain","2.0"),
                    EntityModelId.build("testDomainAbstract","standardchildtestdocabstractdifferentdomain","1.0"),
                    EntityModelId.build("testDomainChildAbstract","standardgrandchildtestdocabstractdifferentdomain","1.0"),
                    EntityModelId.build("testDomainChildSiblingAbstract","standardgrandchildtestdocsiblingabstractdifferentdomain","1.0")
                );

        EntityDef rootNotAbtract = EntityDef.build(CouchbaseDocumentStructureReflection.getReflectionFromClass((Class)StandardTestDoc.class));
        assertThat(manager.getChildEntities(rootNotAbtract))
                .hasSize(5)
                .extracting("modelId")
                .containsExactlyInAnyOrder(
                        EntityModelId.build("testChild","standardchildtestdocsamedomain","1.0"),
                        EntityModelId.build("testDomainDifferent","standardchildtestdocdifferentdomain","2.0"),
                        EntityModelId.build("testDomainAbstract","standardchildtestdocabstractdifferentdomain","1.0"),
                        EntityModelId.build("testDomainChildAbstract","standardgrandchildtestdocabstractdifferentdomain","1.0"),
                        EntityModelId.build("testDomainChildSiblingAbstract","standardgrandchildtestdocsiblingabstractdifferentdomain","1.0")
                    );

        assertThat(manager.getEffectiveDomains(rootNotAbtract))
                .hasSize(5)
                .containsExactlyInAnyOrder(
                        "test",
                        "testChild",
                        "testDomainDifferent","testDomainChildAbstract","testDomainChildSiblingAbstract");

        EntityDef rootAbstract = EntityDef.build(CouchbaseDocumentStructureReflection.getReflectionFromClass((Class)StandardChildTestDocAbstractDifferentDomain.class));
        assertThat(manager.getChildEntities(rootAbstract))
                .hasSize(2)
                .extracting("modelId")
                .containsExactlyInAnyOrder(
                        EntityModelId.build("testDomainChildAbstract","standardgrandchildtestdocabstractdifferentdomain","1.0"),
                        EntityModelId.build("testDomainChildSiblingAbstract","standardgrandchildtestdocsiblingabstractdifferentdomain","1.0")
                );

        assertThat(manager.getEffectiveDomains(rootAbstract))
                .hasSize(2)
                .containsExactlyInAnyOrder("testDomainChildAbstract","testDomainChildSiblingAbstract");

    }

}