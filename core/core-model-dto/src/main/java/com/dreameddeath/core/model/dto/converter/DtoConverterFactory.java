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

package com.dreameddeath.core.model.dto.converter;

import com.dreameddeath.core.java.utils.StringUtils;
import com.dreameddeath.core.model.dto.converter.model.DtoConverterDef;
import com.dreameddeath.core.model.entity.EntityDefinitionManager;
import com.google.common.base.Preconditions;

import java.lang.reflect.Modifier;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by christophe jeunesse on 22/03/2017.
 */
public class DtoConverterFactory {
    private final DtoConverterManager dtoConverterManager;
    private final EntityDefinitionManager entityDefinitionManager;
    private final Map<Entry,IDtoInputConverter> inputConverterMap = new ConcurrentHashMap<>();
    private final Map<Entry,IDtoOutputConverter> outputConverterMap = new ConcurrentHashMap<>();
    private final Map<Entry,IDtoInputMapper> inputMapperMap = new ConcurrentHashMap<>();
    private final Map<Entry,IDtoOutputMapper> outputMapperMap = new ConcurrentHashMap<>();


    private void initConverters(){
        for(DtoConverterDef converterDef:dtoConverterManager.getCachedConvertersDef()){
            try {
                Class docClass = Thread.currentThread().getContextClassLoader().loadClass(converterDef.getEntityClassName());
                @SuppressWarnings("unchecked")
                Class<? extends IDtoFactoryAware> converterClass = (Class<? extends IDtoFactoryAware>)Thread.currentThread().getContextClassLoader().loadClass(converterDef.getConverterClass());
                IDtoFactoryAware converter = converterClass.newInstance();
                if(StringUtils.isNotEmpty(converterDef.getInputClass())){
                    @SuppressWarnings("unchecked")
                    Class inputClass = Thread.currentThread().getContextClassLoader().loadClass(converterDef.getInputClass());
                    Preconditions.checkArgument(converter instanceof IDtoInputMapper,"The class %s doesn't have a mapper",inputClass);
                    inputMapperMap.put(new Entry(docClass,inputClass,converterDef.getInputVersion()),(IDtoInputMapper)converter);


                    IDtoInputConverter inputConverter;
                    if(Modifier.isAbstract(inputClass.getModifiers())){
                        inputConverter = new DtoGenericInputAbstractConverter(docClass,inputClass,converterDef.getInputVersion());
                    }
                    else{
                        Preconditions.checkArgument(converter instanceof IDtoInputConverter,"The class %s hasn't an input converter while not abstract",inputClass);
                        inputConverter = (IDtoInputConverter)converter;
                    }
                    inputConverterMap.put(new Entry(docClass,inputClass,converterDef.getInputVersion()),inputConverter);
                }
                if(StringUtils.isNotEmpty(converterDef.getOutputClass())){
                    @SuppressWarnings("unchecked")
                    Class outputClass = Thread.currentThread().getContextClassLoader().loadClass(converterDef.getOutputClass());

                    Preconditions.checkArgument(converter instanceof IDtoOutputMapper,"The class %s doesn't have a mapper",outputClass);
                    outputMapperMap.put(new Entry(docClass,outputClass,converterDef.getOutputVersion()),(IDtoOutputMapper)converter);

                    IDtoOutputConverter outputConverter;
                    if(Modifier.isAbstract(outputClass.getModifiers())){
                        outputConverter = new DtoGenericOutputAbstractConverter(docClass,outputClass,converterDef.getOutputVersion());
                    }
                    else{
                        Preconditions.checkArgument(converter instanceof IDtoOutputConverter,"The class %s hasn't an output converter while not abstract",outputClass);
                        outputConverter = (IDtoOutputConverter)converter;
                    }
                    outputConverterMap.put(new Entry(docClass,outputClass,converterDef.getOutputVersion()),outputConverter);
                }
            }
            catch(ClassNotFoundException|InstantiationException|IllegalAccessException e){
                throw new RuntimeException("Unexpected error for converter "+converterDef,e);
            }
        }

        outputMapperMap.values().forEach(elt->elt.setDtoConverterFactory(this));
        inputMapperMap.values().forEach(elt->elt.setDtoConverterFactory(this));
        outputConverterMap.values().forEach(elt->elt.setDtoConverterFactory(this));
        inputConverterMap.values().forEach(elt->elt.setDtoConverterFactory(this));
    }
    
    public DtoConverterFactory(DtoConverterManager manager,EntityDefinitionManager entityDefinitionManager){
        this.dtoConverterManager = manager;
        this.entityDefinitionManager = entityDefinitionManager;
        initConverters();
    }

    public DtoConverterFactory(){
        this(new DtoConverterManager(),new EntityDefinitionManager());
    }

    public DtoConverterManager getDtoConverterManager() {
        return dtoConverterManager;
    }

    public EntityDefinitionManager getEntityDefinitionManager() {
        return entityDefinitionManager;
    }

    public <TDOC,TINPUT> IDtoInputConverter<TDOC,TINPUT> getDtoInputConverter(Class<TDOC> internalClass, Class<TINPUT> inputClass){
        return getDtoInputConverter(internalClass,inputClass,null);
    }

    public <TDOC,TINPUT> IDtoInputConverter<TDOC,TINPUT> getDtoInputConverter(Class<TDOC> internalClass, Class<TINPUT> inputClass,String version){
        final Entry requestedEntry=new Entry(internalClass,inputClass,version);
        return inputConverterMap.computeIfAbsent(requestedEntry, entry -> getBestMatching(inputConverterMap,entry));
    }


    public <TDOC,TOUTPUT> IDtoOutputConverter<TDOC,TOUTPUT> getDtoOutputConverter(Class<TDOC> internalClass,Class<TOUTPUT> outputClass){
        return getDtoOutputConverter(internalClass,outputClass,null);
    }


    public <TDOC,TOUTPUT> IDtoOutputConverter<TDOC,TOUTPUT> getDtoOutputConverter(Class<TDOC> internalClass,Class<TOUTPUT> outputClass,String version){
        final Entry requestedEntry=new Entry(internalClass,outputClass,version);
        return outputConverterMap.computeIfAbsent(requestedEntry, entry -> getBestMatching(outputConverterMap,entry));
    }


    public <TDOC,TINPUT> IDtoInputMapper<TDOC,TINPUT> getDtoInputMapper(Class<TDOC> internalClass, Class<TINPUT> inputClass){
        return getDtoInputMapper(internalClass,inputClass,null);
    }

    public <TDOC,TINPUT> IDtoInputMapper<TDOC,TINPUT> getDtoInputMapper(Class<TDOC> internalClass, Class<TINPUT> inputClass,String version){
        final Entry requestedEntry=new Entry(internalClass,inputClass,version);
        return inputMapperMap.computeIfAbsent(requestedEntry, entry -> getBestMatching(inputMapperMap,entry));
    }


    public <TDOC,TOUTPUT> IDtoOutputMapper<TDOC,TOUTPUT> getDtoOutputMapper(Class<TDOC> internalClass,Class<TOUTPUT> outputClass){
        return getDtoOutputMapper(internalClass, outputClass,null);
    }

    public <TDOC,TOUTPUT> IDtoOutputMapper<TDOC,TOUTPUT> getDtoOutputMapper(Class<TDOC> internalClass,Class<TOUTPUT> outputClass,String version){
        final Entry requestedEntry=new Entry(internalClass,outputClass,version);
        return outputMapperMap.computeIfAbsent(requestedEntry, entry -> getBestMatching(outputMapperMap,entry));
    }


    private <TMAPPED> TMAPPED getBestMatching(final Map<Entry,TMAPPED> entryMap,final Entry wishedEntry){
        Optional<Entry> foundEntry = entryMap.keySet().stream()
                .filter(currEntry->currEntry.type.equals(wishedEntry.type))
                .filter(currEntry->currEntry.doc.equals(wishedEntry.doc))
                .filter(currEntry->currEntry.isValidVersion(wishedEntry))
                .sorted(Entry::compareClassesAndReversedVersion)
                .findFirst();

        Preconditions.checkArgument(foundEntry.isPresent(),"Cannot find entry %s",wishedEntry);
        return entryMap.get(foundEntry.get());
    }


    private static class Entry{
        private final static Pattern versionPattern = Pattern.compile("^(\\d+)(?:\\.(\\d+)(?:\\.(\\d+))?)");
        private final Class<?> doc;
        private final Class<?> type;
        private final Integer majorVersion;
        private final Integer minorVersion;
        private final Integer patchVersion;

        public Entry(Class<?> doc, Class<?> type, String version) {
            this.doc = doc;
            this.type = type;
            if (StringUtils.isEmpty(version)) {
                majorVersion = null;
                minorVersion = null;
                patchVersion = null;
            } else {
                Matcher matcher = versionPattern.matcher(version);
                Preconditions.checkArgument(matcher.matches(), "The version %s for doc %s and type %s isn't valid", version, doc, type);
                majorVersion = Integer.parseInt(matcher.group(1));
                minorVersion = (matcher.groupCount() < 1 && StringUtils.isEmpty(matcher.group(2))) ? null : Integer.parseInt(matcher.group(1));
                patchVersion = (matcher.groupCount() < 2 && StringUtils.isEmpty(matcher.group(3))) ? null : Integer.parseInt(matcher.group(2));
            }
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            Entry entry = (Entry) o;

            if (!doc.equals(entry.doc)) return false;
            if (!type.equals(entry.type)) return false;
            if (majorVersion != null ? !majorVersion.equals(entry.majorVersion) : entry.majorVersion != null)
                return false;
            if (minorVersion != null ? !minorVersion.equals(entry.minorVersion) : entry.minorVersion != null)
                return false;
            return patchVersion != null ? patchVersion.equals(entry.patchVersion) : entry.patchVersion == null;
        }

        @Override
        public int hashCode() {
            int result = doc.hashCode();
            result = 31 * result + type.hashCode();
            result = 31 * result + (majorVersion != null ? majorVersion.hashCode() : 0);
            result = 31 * result + (minorVersion != null ? minorVersion.hashCode() : 0);
            result = 31 * result + (patchVersion != null ? patchVersion.hashCode() : 0);
            return result;
        }

        public boolean isValidEntry(Entry wishedEntry){
            return doc.equals(wishedEntry.doc) &&
                    type.equals(wishedEntry.type) &&
                    isValidVersion(wishedEntry);
        }


        private boolean isValidVersion(Entry wishedEntry){
            Boolean isValid = isValidVersion(majorVersion,wishedEntry.majorVersion);
            return isValid!=null && isValid;
        }


        private Boolean isValidVersion(Integer currVersion,Integer wishedVersion){
            if(wishedVersion==null || currVersion==null || currVersion.equals(wishedVersion)){
                return true;
            }
            return null;
        }


        //Put the deepest in hierarchy first
        public int compareClassInheritance(Class<?> a,Class<?> b){
            if(a==b){
                return 0;
            }
            else if(a.isAssignableFrom(b)){
                return -1;
            }
            else{
                return 1;
            }
        }

        public int compareReversedVersion(Entry b) {
            int res;
            res=compareVersion(this.majorVersion,b.majorVersion);
            if(res==0){
                res = compareVersion(this.minorVersion,this.minorVersion);
            }
            if(res==0){
                res = compareVersion(this.patchVersion,this.patchVersion);
            }
            return -res;
        }

        private int compareVersion(Integer current,Integer secondary){
            if(current!=null && secondary!=null){
                return current.compareTo(secondary);
            }
            else if(current==null && secondary==null){
                return 0;
            }
            else if(secondary==null){
                return 1;
            }
            else{
                return -1;
            }
        }

        @Override
        public String toString() {
            return "Entry{" +
                    "doc=" + doc +
                    ", type=" + type +
                    ", majorVersion=" + majorVersion +
                    ", minorVersion=" + minorVersion +
                    ", patchVersion=" + patchVersion +
                    '}';
        }

        public int compareClassesAndReversedVersion(Entry b) {
            int res=compareClassInheritance(this.doc,b.doc);
            if(res!=0){
                res=compareClassInheritance(this.type,b.type);
            }
            if(res!=0){
                res = compareReversedVersion(b);
            }

            return res;
        }
    }
}
