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

package com.dreameddeath.core.dao.service.annotation.processor;

import com.dreameddeath.compile.tools.annotation.processor.reflection.AnnotationInfo;
import com.dreameddeath.core.dao.annotation.processor.IDaoGeneratorPlugin;
import com.dreameddeath.core.dao.annotation.processor.model.GlobalDef;
import com.dreameddeath.core.dao.service.annotation.service.DiscardDaoRestGeneration;
import com.dreameddeath.core.dao.service.annotation.service.RestDao;

import javax.annotation.processing.Messager;
import javax.lang.model.element.Element;

/**
 * Created by christophe jeunesse on 31/01/2017.
 */
public class AddRestDaoAnnotationGeneratorPlugin implements IDaoGeneratorPlugin {
    @Override
    public void manage(GlobalDef globalDef, Messager messager, Element element) {
        if(globalDef.getEntity().getDocReflection().getClassInfo().getAnnotation(DiscardDaoRestGeneration.class)==null){
            globalDef.getDaoDef().addPluginImportName(AnnotationInfo.getClassInfo(RestDao.class).getImportName());
            globalDef.getDaoDef().addPluginAnnotationStrings("@RestDao("+
                    "domain=\""+globalDef.getEntity().getDomain()+"\",\n"+
                    "name=\""+globalDef.getEntity().getDbName()+"\",\n"+
                    "version=\""+globalDef.getEntity().getVersion()+"\",\n"+
                    "rootPath=\""+globalDef.getEntity().getDbName()+"\",\n"+
                    "uidName=\""+globalDef.getEntity().getDbName()+"Uid\",\n"+
                    "dbPath=\""+globalDef.getDbPathDef().getBasePath()+"\",\n"+
                    "parentEntityClassName=\""+globalDef.getEntity().getParentEntityClassName()+"\""+
                ")"
            );

        }
    }
}
