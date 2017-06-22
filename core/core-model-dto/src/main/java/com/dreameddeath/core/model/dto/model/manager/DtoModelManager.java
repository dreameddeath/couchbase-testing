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

package com.dreameddeath.core.model.dto.model.manager;

import com.dreameddeath.core.java.utils.StringUtils;
import com.dreameddeath.core.json.ObjectMapperFactory;
import com.dreameddeath.core.model.dto.converter.DtoConverterManager;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Created by ceaj8230 on 07/03/2017.
 */
public class DtoModelManager {
    public static final String ROOT_PATH= DtoConverterManager.ROOT_PATH;
    public static final String MODELS_DEF_PATH ="models";
    private static final Logger LOG = LoggerFactory.getLogger(DtoModelManager.class);

    private final ObjectMapper mapper = ObjectMapperFactory.BASE_INSTANCE.getMapper();
    private final AtomicReference<List<DtoModelDef>> cachedModelDef = new AtomicReference<>();
    private ClassLoader classLoader=null;


    public DtoModelManager(){
        this(null);
    }

    public DtoModelManager(ClassLoader classLoader){
        this.classLoader = classLoader;
    }

    public String getModelDefsPath(DtoModelDef modelDef){
        return String.format("%s/%s/%s/%s.json",
                ROOT_PATH,
                MODELS_DEF_PATH,
                StringUtils.isEmpty(modelDef.getType())?"__UNKNOWN__":modelDef.getType(),
                modelDef.getClassName());
    }

    public void buildModelDefFile(Writer writer, DtoModelDef modelDef) throws IOException {
        mapper.writeValue(writer,modelDef);
    }

    public synchronized List<DtoModelDef> getModelsDefs(){
        try {
            PathMatchingResourcePatternResolver resolver = classLoader!=null?new PathMatchingResourcePatternResolver(classLoader):new PathMatchingResourcePatternResolver();
            Resource[] resultResources= resolver.getResources("classpath*:" + ROOT_PATH + "/" + MODELS_DEF_PATH + "/*/*.json");
            List<DtoModelDef> result = new ArrayList<>(resultResources.length);
            for(Resource entityResource:resultResources){
                try {
                    result.add(mapper.readValue(entityResource.getInputStream(), DtoModelDef.class));
                }
                catch(Throwable e){
                    LOG.error("Cannot read entity file <"+entityResource.getFile().getAbsolutePath()+">",e);
                    throw new RuntimeException("Cannot read entity file <"+entityResource.getFilename()+">",e);
                }
            }
            return result;
        }
        catch(Exception e) {
            throw new RuntimeException(e);
        }
    }

    public List<DtoModelDef> getCachedModelDefs(){
        return cachedModelDef.updateAndGet(list-> {
            if (list == null) {
                return getModelsDefs();
            }
            else{
                return list;
            }
        });
    }

}
