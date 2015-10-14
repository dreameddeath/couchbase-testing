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

package com.dreameddeath.core.model.entity;

import com.dreameddeath.core.model.annotation.DocumentVersionUpgrader;
import com.dreameddeath.core.model.entity.model.EntityModelId;
import com.dreameddeath.core.model.entity.model.EntityVersion;
import com.dreameddeath.core.model.entity.model.IVersionedEntity;
import com.esotericsoftware.reflectasm.MethodAccess;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Created by Christophe Jeunesse on 27/11/2014.
 */
public class EntityVersionUpgradeManager {
    public static final String DOCUMENT_DEF_PATH="DocumentDef";
    public static final String DOCUMENT_UPGRADE_PATH="DocumentUpgrade";

    private Set<String> discardUpgrades=new HashSet<>();
    private Map<String,UpgradeMethodWrapper> versionUpgraderMap = new HashMap<>();

    public void addVersionToDiscard(String domain,String name,String version){
        discardUpgrades.add(EntityModelId.build(domain, name, version).toString());
    }

    public void removeVersionToDiscard(String domain,String name,String version){
        discardUpgrades.remove(EntityModelId.build(domain, name, version).toString());
    }

    public String getDocumentVersionUpgraderFilename(EntityModelId modelId){
        return String.format("%s/%s/%s.%s/v%s.%s",
                EntityDefinitionManager.ROOT_PATH, DOCUMENT_UPGRADE_PATH,
                modelId.getDomain(),
                modelId.getName(),
                modelId.getEntityVersion().getMajor(),
                modelId.getEntityVersion().getMinor());
    }

    public String getFilename(DocumentVersionUpgrader annotation){
        return getDocumentVersionUpgraderFilename(EntityModelId.build(annotation.domain(), annotation.name(), annotation.from()));
    }

    public String buildTargetVersion(DocumentVersionUpgrader annotation){
        EntityVersion targetVersion = EntityVersion.version(annotation.to());
        return String.format("%s/%s/%s.%s.%s", annotation.domain(), annotation.name(), targetVersion.getMajor(), targetVersion.getMinor(), targetVersion.getPatch());
    }


    public UpgradeMethodWrapper getUpgraderReference(EntityModelId modelId){
        String typeId = modelId.toString();

        if(! versionUpgraderMap.containsKey(typeId)){
            String filename = getDocumentVersionUpgraderFilename(modelId);
            if(Thread.currentThread().getContextClassLoader().getResource(filename)==null){
                versionUpgraderMap.put(typeId,null);
            }
            else{
                InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream(filename);
                BufferedReader fileReader = new BufferedReader(new InputStreamReader(is));
                try {
                    String fullContent = fileReader.readLine();
                    String[] parts=fullContent.split(";");
                    EntityModelId targetModelId=EntityModelId.build(parts[2]);

                    versionUpgraderMap.put(typeId,
                                new UpgradeMethodWrapper(
                                        Thread.currentThread().getContextClassLoader().loadClass(parts[0]),
                                        parts[1],
                                        getUpgraderReference(targetModelId),
                                        typeId,
                                        parts[2]
                                )
                        );
                }
                catch(ClassNotFoundException|IOException e){
                    throw  new RuntimeException("Cannot find/read file <"+filename+"> for id <"+typeId+">",e);
                }
            }
        }
        return versionUpgraderMap.get(typeId);
    }

    public Object performUpgrade(Object obj,EntityModelId modelId){
        UpgradeMethodWrapper updateMethod = getUpgraderReference(modelId);
        Object res = obj;
        while(updateMethod!=null){
            if(discardUpgrades.contains(updateMethod.getTargetTypeId())) break;
            res = updateMethod.invoke(obj);
            if(res instanceof IVersionedEntity){
                ((IVersionedEntity)res).setDocumentFullVersionId(updateMethod.getTargetTypeId());
            }
            updateMethod = updateMethod.getNextWrapper();
        }
        return res;
    }

    public static class UpgradeMethodWrapper {
        private final Object upgraderObject;
        private final MethodAccess access;
        private final int index;
        private final UpgradeMethodWrapper nextWrapper;
        private final String targetTypeId;
        private final String sourceTypeId;

        public UpgradeMethodWrapper(Class clazz, String method, UpgradeMethodWrapper nextWapper,String sourceTypeId,String targetTypeId){
            try{
                upgraderObject = clazz.newInstance();
            }
            catch(Throwable e){
                throw new RuntimeException("Cannot instantiate class "+clazz.getName());
            }
            this.sourceTypeId = sourceTypeId;
            access = MethodAccess.get(clazz);
            Class foundClass = EntityDefinitionManager.getInstance().findClassFromVersionnedTypeId(sourceTypeId);
            index = access.getIndex(method,foundClass);
            nextWrapper = nextWapper;
            this.targetTypeId = targetTypeId;
        }

        public Object invoke(Object... var2){
            return access.invoke(upgraderObject,index,var2);
        }

        public UpgradeMethodWrapper getNextWrapper(){
            return nextWrapper;
        }
        public String getTargetTypeId(){return targetTypeId;}
        public String getSourceTypeId(){return sourceTypeId;}
    }

}
