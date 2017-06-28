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

package com.dreameddeath.core.model.dto.model;

/**
 * Created by christophe jeunesse on 02/02/2017.
 */
public interface ICouchbaseDocumentDto {
    IDtoMetaData getMeta();
    void setMeta(IDtoMetaData meta);

    interface IDtoMetaData {
        String domain();
        String name();
        String key();
        String version();
        String cas();
    }
}
