/*
 * Copyright Christophe Jeunesse
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */

package com.dreameddeath.core.dao.model;


import com.dreameddeath.core.model.annotation.DocumentProperty;
import com.dreameddeath.core.model.document.CouchbaseDocumentElement;
import com.dreameddeath.core.model.property.Property;
import com.dreameddeath.core.model.property.impl.ImmutableProperty;


public class TestGeneratedDaoLink extends CouchbaseDocumentElement{
    private TestGeneratedDao value;
    /**
     * key : parent key
     */
    @DocumentProperty("key")
    private Property<String> key = new ImmutableProperty<>(TestGeneratedDaoLink.this);

    /**
     * Getter for property key
     * @return The current value
     */
    public String getKey(){
        String actualKey = key.get();
        if(actualKey==null && value!=null){
            setKey(value.getBaseMeta().getKey());
        }
        return key.get();
    }

    /**
     * Setter for property key
     * @param newValue  the new value for the property
     */
    public void setKey(String newValue){
        key.set(newValue);
    }

    public TestGeneratedDaoLink(){}
    public TestGeneratedDaoLink(String parentKey){
        key.set(parentKey);
    }
    public TestGeneratedDaoLink (TestGeneratedDao src){
        if(src.getBaseMeta().getKey()==null) {
            value = src;
        }
        else{
            setKey(src.getBaseMeta().getKey());
        }
    }
    public TestGeneratedDaoLink(TestGeneratedDaoLink srcLink){this(srcLink.key.get());}
}