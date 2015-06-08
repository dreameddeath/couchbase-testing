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

package com.dreameddeath.core.model.mapper.impl;

import com.dreameddeath.core.model.mapper.IDocumentClassMappingInfo;
import com.dreameddeath.core.model.mapper.IKeyMappingInfo;

/**
 * Created by Christophe Jeunesse on 08/06/2015.
 */
public class KeyMappingInfo implements IKeyMappingInfo {
    private final String _prefix;
    private final String _key;
    private final String _fullKey;
    private final IDocumentClassMappingInfo _documentClassMappingInfo;

    protected KeyMappingInfo(String prefix,String key,String fullKey,IDocumentClassMappingInfo classMappingInfo){
        _prefix = prefix;
        _key = key;
        _fullKey = fullKey;
        _documentClassMappingInfo = classMappingInfo;
    }

    @Override
    public String prefix() {
        return _prefix;
    }

    @Override
    public String key() {
        return _key;
    }

    @Override
    public String fullKey() {
        return _fullKey;
    }

    @Override
    public IDocumentClassMappingInfo classMappingInfo() {
        return _documentClassMappingInfo;
    }
}
