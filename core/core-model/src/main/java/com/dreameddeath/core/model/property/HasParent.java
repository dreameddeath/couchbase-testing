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

package com.dreameddeath.core.model.property;


import com.dreameddeath.core.model.document.CouchbaseDocument;

/**
 * Created by Christophe Jeunesse on 07/09/2014.
 */
public interface HasParent {
    HasParent getParentElement();
    void setParentElement(HasParent parent);

    class Helper {
        public static <T extends HasParent> T getFirstParentOfClass(HasParent src, Class<T> clazz) {
            if (src != null) {
                if (clazz.isAssignableFrom(src.getClass())) {
                    @SuppressWarnings("unchecked")
                    T result = (T) src;
                    return result;
                } else {
                    return getFirstParentOfClass(src.getParentElement(), clazz);
                }
            }
            return null;
        }

        public static CouchbaseDocument getParentDocument(HasParent src){
            if (src != null) {
                if (src instanceof CouchbaseDocument) {
                    return (CouchbaseDocument) (src);
                } else {
                    return getParentDocument(src.getParentElement());
                }
            }
            return null;
        }

        public static void dirtyParentDocument(HasParent src){
            CouchbaseDocument doc = getParentDocument(src);
            if(doc!=null){
                doc.getBaseMeta().setStateDirty();
            }
        }
    }
}