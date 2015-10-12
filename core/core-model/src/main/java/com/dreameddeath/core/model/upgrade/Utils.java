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

package com.dreameddeath.core.model.upgrade;

import com.dreameddeath.core.model.annotation.DocumentVersionUpgrader;
import com.dreameddeath.core.model.entity.EntityModelId;
import com.dreameddeath.core.model.entity.EntityVersion;
import com.dreameddeath.core.model.entity.IVersionedDocument;
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
public class Utils {
    private static String ROOT_PATH="META-INF/core-annotation";
    private static String DOCUMENT_DEF_PATH="DocumentDef";
    private static String DOCUMENT_UPGRADE_PATH="DocumentUpgrade";

    private static Set<String> _discardUpgrades=new HashSet<>();
    private static Map<String,Class> _versionClassMap=new HashMap<String,Class>();
    private static Map<String,UpgradeMethodWrapper> _versionUpgraderMap = new HashMap<String,UpgradeMethodWrapper>();

    public static void addVersionToDiscard(String domain,String name,String version){
        _discardUpgrades.add(EntityModelId.build(domain, name, version).toString());
    }

    public static void removeVersionToDiscard(String domain,String name,String version){
        _discardUpgrades.remove(EntityModelId.build(domain, name, version).toString());
    }

    public static class UpgradeMethodWrapper {
        private final Object _upgraderObject;
        private final MethodAccess _access;
        private final int _index;
        private final UpgradeMethodWrapper _nextWrapper;
        private final String _targetTypeId;
        private final String _sourceTypeId;
        public UpgradeMethodWrapper(Class clazz, String method, UpgradeMethodWrapper nextWapper,String sourceTypeId,String targetTypeId){
            try{
                _upgraderObject = clazz.newInstance();
            }
            catch(Throwable e){
                throw new RuntimeException("Cannot instantiate class "+clazz.getName());
            }
            _sourceTypeId = sourceTypeId;
            _access = MethodAccess.get(clazz);
            Class foundClass = findClassFromVersionnedTypeId(_sourceTypeId);
            _index = _access.getIndex(method,foundClass);
            _nextWrapper = nextWapper;
            _targetTypeId = targetTypeId;
        }

        public Object invoke(Object... var2){
            return _access.invoke(_upgraderObject,_index,var2);
        }

        public UpgradeMethodWrapper getNextWrapper(){
            return _nextWrapper;
        }
        public String getTargetTypeId(){return _targetTypeId;}
        public String getSourceTypeId(){return _sourceTypeId;}
    }

    public static String getDocumentVersionUpgraderFilename(EntityModelId modelId){
        return String.format("%s/%s/%s.%s/v%s.%s",
                ROOT_PATH, DOCUMENT_UPGRADE_PATH,
                modelId.getDomain(),
                modelId.getName(),
                modelId.getEntityVersion().getMajor(),
                modelId.getEntityVersion().getMinor());
    }


    public static String getDocumentEntityFilename(EntityModelId modelId){
        return String.format("%s/%s/%s.%s/v%s", ROOT_PATH, DOCUMENT_DEF_PATH,
                modelId.getDomain(),
                modelId.getName(),
                modelId.getEntityVersion().getMajor());
    }

    public static String getFilename(DocumentVersionUpgrader annotation){
        return getDocumentVersionUpgraderFilename(EntityModelId.build(annotation.domain(), annotation.name(), annotation.from()));
    }

    public static String buildTargetVersion(DocumentVersionUpgrader annotation){
        EntityVersion targetVersion = EntityVersion.version(annotation.to());
        return String.format("%s/%s/%s.%s.%s", annotation.domain(), annotation.name(), targetVersion.getMajor(), targetVersion.getMinor(), targetVersion.getPatch());
    }

    public static Class findClassFromVersionnedTypeId(String typeId){
        if(!_versionClassMap.containsKey(typeId)){
            EntityModelId modelId = EntityModelId.build(typeId);

            //it will naturally exclude patch from typeId
            String filename = Utils.getDocumentEntityFilename(modelId);
            InputStream is =Thread.currentThread().getContextClassLoader().getResourceAsStream(filename);
            if(is==null){
                throw  new RuntimeException("Cannot find/read file <"+filename+"> for id <"+typeId+">");
            }
            BufferedReader fileReader = new BufferedReader(new InputStreamReader(is));
            try {
                String className = fileReader.readLine();
                _versionClassMap.put(typeId, Thread.currentThread().getContextClassLoader().loadClass(className));
            }
            catch(ClassNotFoundException|IOException e){
                throw  new RuntimeException("Cannot find/read file <"+filename+"> for id <"+typeId+">",e);
            }
        }
        return _versionClassMap.get(typeId);
    }

    public static UpgradeMethodWrapper getUpgraderReference(EntityModelId modelId){
        String typeId = modelId.toString();

        if(! _versionUpgraderMap.containsKey(typeId)){
            String filename = Utils.getDocumentVersionUpgraderFilename(modelId);
            if(Thread.currentThread().getContextClassLoader().getResource(filename)==null){
                _versionUpgraderMap.put(typeId,null);
            }
            else{
                InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream(filename);
                BufferedReader fileReader = new BufferedReader(new InputStreamReader(is));
                try {
                    String fullContent = fileReader.readLine();
                    String[] parts=fullContent.split(";");
                    EntityModelId targetModelId=EntityModelId.build(parts[2]);

                    _versionUpgraderMap.put(typeId,
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
        return _versionUpgraderMap.get(typeId);
    }

    public static Object performUpgrade(Object obj,EntityModelId modelId){
        UpgradeMethodWrapper updateMethod = getUpgraderReference(modelId);
        Object res = obj;
        while(updateMethod!=null){
            if (_discardUpgrades.contains(updateMethod.getTargetTypeId()))break;
            res = updateMethod.invoke(obj);
            if(res instanceof IVersionedDocument){
                ((IVersionedDocument)res).setDocumentFullVersionId(updateMethod.getTargetTypeId());
            }
            updateMethod = updateMethod.getNextWrapper();
        }
        return res;
    }
}
