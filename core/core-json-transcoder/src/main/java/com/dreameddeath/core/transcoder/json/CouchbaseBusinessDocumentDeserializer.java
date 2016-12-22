/*
 *
 *  * Copyright Christophe Jeunesse
 *  *
 *  *    Licensed under the Apache License, Version 2.0 (the "License");
 *  *    you may not use this file except in compliance with the License.
 *  *    You may obtain a copy of the License at
 *  *
 *  *      http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  *    Unless required by applicable law or agreed to in writing, software
 *  *    distributed under the License is distributed on an "AS IS" BASIS,
 *  *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  *    See the License for the specific language governing permissions and
 *  *    limitations under the License.
 *
 */

package com.dreameddeath.core.transcoder.json;

import com.dreameddeath.core.java.utils.StringUtils;
import com.dreameddeath.core.model.document.CouchbaseDocument;
import com.dreameddeath.core.model.entity.EntityVersionUpgradeManager;
import com.dreameddeath.core.model.entity.model.EntityModelId;
import com.dreameddeath.core.model.entity.model.IVersionedEntity;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.BeanDescription;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.deser.BeanDeserializer;
import com.fasterxml.jackson.databind.deser.BeanDeserializerBase;
import com.fasterxml.jackson.databind.deser.BeanDeserializerBuilder;
import com.fasterxml.jackson.databind.deser.SettableBeanProperty;
import com.fasterxml.jackson.databind.deser.impl.BeanPropertyMap;
import com.fasterxml.jackson.databind.deser.impl.ObjectIdReader;
import com.fasterxml.jackson.databind.util.NameTransformer;
import com.google.common.base.Preconditions;

import java.io.IOException;
import java.util.HashSet;
import java.util.Map;

/**
 * Created by Christophe Jeunesse on 28/11/2014.
 */
public class CouchbaseBusinessDocumentDeserializer extends BeanDeserializer {
    private EntityVersionUpgradeManager entityVersionUpgradeManager =null;
    /**
     * Constructor used by {@link com.fasterxml.jackson.databind.deser.BeanDeserializerBuilder}.
     */
    public CouchbaseBusinessDocumentDeserializer(BeanDeserializerBuilder builder,
                                                 BeanDescription beanDesc,
                                                 BeanPropertyMap properties, Map<String, SettableBeanProperty> backRefs,
                                                 HashSet<String> ignorableProps, boolean ignoreAllUnknown,
                                                 boolean hasViews) {
        super(builder, beanDesc, properties, backRefs,
                ignorableProps, ignoreAllUnknown, hasViews);
    }

    /**
     * Copy-constructor that can be used by sub-classes to allow
     * copy-on-write style copying of settings of an existing instance.
     */
    protected CouchbaseBusinessDocumentDeserializer(BeanDeserializerBase src) {
        super(src);
    }

    protected CouchbaseBusinessDocumentDeserializer(BeanDeserializerBase src, boolean ignoreAllUnknown) {
        super(src, ignoreAllUnknown);
    }

    protected CouchbaseBusinessDocumentDeserializer(BeanDeserializerBase src, NameTransformer unwrapper) {
        super(src, unwrapper);
    }

    public CouchbaseBusinessDocumentDeserializer(BeanDeserializerBase src, ObjectIdReader oir) {
        super(src, oir);
    }

    public CouchbaseBusinessDocumentDeserializer(BeanDeserializerBase src, HashSet<String> ignorableProps) {
        super(src, ignorableProps);
    }

    @Override
    public Object deserialize(JsonParser jp, DeserializationContext ctxt)
            throws IOException {
        Object res = super.deserialize(jp, ctxt);
        if(res instanceof IVersionedEntity){
            String versionTypeId = ((IVersionedEntity) res).getDocumentFullVersionId();
            String key = ((res instanceof CouchbaseDocument)?((CouchbaseDocument) res).getBaseMeta().getKey():null);
            Preconditions.checkArgument(StringUtils.isNotEmpty(versionTypeId),"The version id is not defined for element {} with key {}",res.getClass(),key);
            if(entityVersionUpgradeManager ==null){
                entityVersionUpgradeManager =(EntityVersionUpgradeManager)ctxt.getConfig().getAttributes().getAttribute(EntityVersionUpgradeManager.class);
                if(entityVersionUpgradeManager ==null){
                    entityVersionUpgradeManager =new EntityVersionUpgradeManager();
                }
            }
            res = entityVersionUpgradeManager.performUpgrade(res, EntityModelId.build(versionTypeId));
        }
        return res;
    }

    @Override
    public Object deserialize(JsonParser jp, DeserializationContext ctxt, Object bean)
            throws IOException {
        Object res = super.deserialize(jp, ctxt, bean);
        return res;

    }

    @Override
    public Object deserializeFromObject(JsonParser jp, DeserializationContext ctxt)
            throws IOException {
        Object res = super.deserializeFromObject(jp, ctxt);
        return res;
    }

    @Override
    public JsonDeserializer<Object> unwrappingDeserializer(NameTransformer unwrapper) {
    /* bit kludgy but we don't want to accidentally change type; sub-classes
         * MUST override this method to support unwrapped properties...
         */
        if (getClass() != CouchbaseBusinessDocumentDeserializer.class) {
            return this;
        }
        /* main thing really is to just enforce ignoring of unknown
         * properties; since there may be multiple unwrapped values
         * and properties for all may be interleaved...
         */
        return new CouchbaseBusinessDocumentDeserializer(this, unwrapper);
    }

}