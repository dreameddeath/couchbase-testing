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

package com.dreameddeath.core.elasticsearch;

import com.dreameddeath.core.model.document.CouchbaseDocument;

/**
 * Created by Christophe Jeunesse on 03/06/2015.
 */
public interface IElasticSearchMapper {
    String documentIndexBuilder(String bucketName, String key);
    String documentIndexBuilder(String bucketName, Class<? extends CouchbaseDocument> clazz);
    String documentTypeBuilder(String bucketName, String key);
    String documentTypeBuilder(String bucketName, Class<? extends CouchbaseDocument> clazz);

    String documentMappingsBuilder(Class<? extends CouchbaseDocument> clazz);

}
