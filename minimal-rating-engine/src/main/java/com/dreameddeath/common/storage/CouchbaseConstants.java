package com.dreameddeath.common.storage;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public interface CouchbaseConstants {

    public static enum DocumentFlag {
        CdrBucket(0x01),
        CdrCompacted(0x02),
        Compressed(0x100);
        
        private int _value;
        
        DocumentFlag(int value){
            this._value = value;
        }
        
        public int toInteger(){
            return _value;
        }
        
        @Override
        public String toString(){
            return String.format("%s(0x%X)",super.toString(),_value);
        }
        
        static public Set<DocumentFlag> unPack(int binValue){
            Set<DocumentFlag> result=new HashSet<DocumentFlag>();
            for(DocumentFlag flag:DocumentFlag.values()){
                if((flag._value & binValue)!=0){
                   result.add(flag); 
                }
            }
            return result;
        }
        
        static public int pack(Collection<DocumentFlag> flags){
            int result = 0;
            for(DocumentFlag flag :flags){
                result|= flag.toInteger();
            }
            return result;
        }
    }


} 