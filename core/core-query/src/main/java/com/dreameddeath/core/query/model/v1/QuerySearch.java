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

package com.dreameddeath.core.query.model.v1;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;

import java.util.Collection;
import java.util.Map;

/**
 * Created by Christophe Jeunesse on 27/12/2016.
 */
public class QuerySearch {
    private SearchType type;
    private final Multimap<String,String> params= ArrayListMultimap.create();


    public QuerySearch(Builder builder) {
        setType(builder.type);
        setParams(params);
    }


    public QuerySearch(SearchType type,Map<String, ? extends Collection<String>> params) {
        setType(type);
        setParamsFromMap(params);
    }

    public QuerySearch() {
    }

    public SearchType getType() {
        return type;
    }

    public void setType(SearchType type) {
        this.type = type;
    }

    public Multimap<String, String> getParams() {
        return Multimaps.unmodifiableMultimap(params);
    }

    public void setParams(Multimap<String,String> params) {
        this.params.clear();
        this.params.putAll(params);
    }


    public void setParamsFromMap(Map<String, ? extends Collection<String>> params) {
        this.params.clear();
        for(Map.Entry<String,? extends Collection<String>> entry:params.entrySet()){
            this.params.putAll(entry.getKey(),entry.getValue());
        }
    }



    public boolean putParam(String key,String value){
        return this.params.put(key,value);
    }

    public enum SearchType{
        VIEW,
        N1QL,
        ELASTICSEARCH
    }

    public static Builder build(SearchType type){
        return new Builder(type);
    }

    public static class Builder{
        private final SearchType type;
        private final Multimap<String,String> params = ArrayListMultimap.create();

        public Builder(SearchType type){
            this.type = type;
        }

        public Builder withParam(String key, String value){
            this.params.put(key,value);
            return this;
        }

        public QuerySearch create(){
            return new QuerySearch(this);
        }
    }
}
