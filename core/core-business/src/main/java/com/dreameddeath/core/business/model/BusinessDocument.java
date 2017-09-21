/*
 * 	Copyright Christophe Jeunesse
 *
 * 	Licensed under the Apache License, Version 2.0 (the "License");
 * 	you may not use this file except in compliance with the License.
 * 	You may obtain a copy of the License at
 *
 * 	http://www.apache.org/licenses/LICENSE-2.0
 *
 * 	Unless required by applicable law or agreed to in writing, software
 * 	distributed under the License is distributed on an "AS IS" BASIS,
 * 	WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * 	See the License for the specific language governing permissions and
 * 	limitations under the License.
 *
 */

package com.dreameddeath.core.business.model;

import com.dreameddeath.core.dao.model.IHasUniqueKeysRef;
import com.dreameddeath.core.dao.session.ICouchbaseSession;
import com.dreameddeath.core.model.annotation.DocumentProperty;
import com.dreameddeath.core.model.dto.annotation.processor.model.FieldGenMode;
import com.dreameddeath.core.model.dto.annotation.processor.model.SuperClassGenMode;
import com.dreameddeath.core.model.entity.model.EntityModelId;
import com.dreameddeath.core.model.entity.model.IVersionedEntity;
import com.dreameddeath.core.model.property.SetProperty;
import com.dreameddeath.core.model.property.impl.HashSetProperty;
import com.dreameddeath.core.process.model.v1.base.AbstractProcessCouchbaseDocument;
import com.dreameddeath.core.query.annotation.QueryExpose;
import com.dreameddeath.core.query.annotation.QueryFieldInfo;
import com.dreameddeath.core.transcoder.json.CouchbaseDocumentTypeIdResolver;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.annotation.JsonTypeIdResolver;
import org.joda.time.DateTime;

import java.util.HashSet;
import java.util.Set;

@JsonTypeInfo(use= JsonTypeInfo.Id.CUSTOM, include= JsonTypeInfo.As.PROPERTY, property="@t",visible = true)
@JsonTypeIdResolver(CouchbaseDocumentTypeIdResolver.class)
@QueryExpose(rootPath = "dummy",defaultOutputFieldMode = FieldGenMode.FILTER,superClassGenMode = SuperClassGenMode.UNWRAP, notDirecltyExposed = true)
public abstract class BusinessDocument extends AbstractProcessCouchbaseDocument implements IVersionedEntity{
    private EntityModelId fullEntityId;
    @JsonSetter("@t") @Override
    public final void setDocumentFullVersionId(String typeId){
        fullEntityId = EntityModelId.build(typeId);
    }
    @Override
    public final String getDocumentFullVersionId(){
        return fullEntityId!=null?fullEntityId.toString():null;
    }
    @Override
    public final EntityModelId getModelId(){
        return fullEntityId;
    }

    @DocumentProperty("docRevision") @QueryFieldInfo
    private Long revision = 0L;
    @DocumentProperty("docLastModDate") @QueryFieldInfo
    private DateTime lastModificationDate;
    /**
     *  docUniqKeys : List of uniqueness Keys attached to this document
     */
    @DocumentProperty("docUniqKeys")
    private SetProperty<String> docUniqKeys = new HashSetProperty<>(BusinessDocument.this);

    public final Long getDocRevision(){ return revision; }
    public final void setDocRevision(Long rev){ revision=rev; }
    public final Long incDocRevision(ICouchbaseSession session){ return (++revision); }

    public final DateTime getDocLastModDate(){ return lastModificationDate; }
    public final void setDocLastModDate(DateTime date){ lastModificationDate=date; }
    public final void updateDocLastModDate(ICouchbaseSession session){ lastModificationDate=session.getCurrentDate(); }

    // DocUniqKeys Accessors
    public final Set<String> getDocUniqKeys() { return docUniqKeys.get(); }
    public final void setDocUniqKeys(Set<String> vals) { docUniqKeys.set(vals); }

    public BusinessDocument(){
        super(null);
        setBaseMeta(BusinessDocument.this.new MetaInfo());
    }

    public MetaInfo getMeta(){
        return (MetaInfo) getBaseMeta();
    }

    public class MetaInfo extends BaseMetaInfo implements IHasUniqueKeysRef {
        private final Set<String> inDbUniqKeys = new HashSet<>();

        private void syncKeyWithDb(){
            inDbUniqKeys.clear();
            inDbUniqKeys.addAll(docUniqKeys.get());
            docUniqKeys.clear();
        }

        @Override
        public void setStateDeleted(){
            //syncKeyWithDb(); voluntary to key in db values up to date
            super.setStateDeleted();
        }

        @Override
        public void setStateSync(){
            syncKeyWithDb();
            super.setStateSync();
        }

        public Set<String> getToBeDeletedUniqueKeys(){
            Set<String> toRemoveKeyList=new HashSet<>(inDbUniqKeys);
            toRemoveKeyList.addAll(docUniqKeys.get());
            return toRemoveKeyList;
        }

        @Override
        public Set<String> getRemovedUniqueKeys(){
            Set<String> removed=new HashSet<>(inDbUniqKeys);
            removed.removeAll(docUniqKeys.get());
            return removed;
        }

        @Override
        public boolean isInDbKey(String key) {
            return inDbUniqKeys.contains(key);
        }

        @Override
        public final boolean addDocUniqKeys(String key){ return docUniqKeys.add(key); }

    }
}
