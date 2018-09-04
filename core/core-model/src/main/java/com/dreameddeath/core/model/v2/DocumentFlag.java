package com.dreameddeath.core.model.v2;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public enum DocumentFlag {
    Binary(0x01),
    Compressed(0x100),
    Deleted(0x200);

    private int value;

    DocumentFlag(int value){
        this.value = value;
    }

    public int toInteger(){
        return value;
    }

    @Override
    public String toString(){
        return String.format("%s(0x%X)",super.toString(),value);
    }

    static public Set<DocumentFlag> unPack(int binValue){
        Set<DocumentFlag> result=new HashSet<>();
        for(DocumentFlag flag: DocumentFlag.values()){
            if((flag.value & binValue)!=0){
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
