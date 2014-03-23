package com.dreameddeath.rating.storage;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Collection;
import java.io.Externalizable;

import com.dreameddeath.common.storage.CouchbaseConstants;

public class RawCdrsMap<T extends RawCdr> extends HashMap<String,T>{
    private Collection<CouchbaseConstants.DocumentFlag> _flags;
    private int _currDbSize;
    private boolean _isIncrementalRating;
    private int _checkSum;
    
    public void setCheckSum(int checkSum){
        _checkSum=checkSum;
    }
    
    public int getCheckSum(){
        return _checkSum;
    }
    
    
    public RawCdrsMap(){
        this(-1,null,false);
    }
    
    public RawCdrsMap(boolean isForIncrementalRating){
        this(-1,null,isForIncrementalRating);
    }
    
    public RawCdrsMap(Collection<CouchbaseConstants.DocumentFlag> flags){
        this(-1,flags,false);
    }
    
    public RawCdrsMap(Collection<CouchbaseConstants.DocumentFlag> flags,boolean isForIncrementalRating){
        this(-1,flags,isForIncrementalRating);
    }
    
    public RawCdrsMap(int size){
        this(size,null,false);
    }
    
    public RawCdrsMap(int size,boolean isForIncrementalRating){
        this(size,null,isForIncrementalRating);
    }
    
    
    public RawCdrsMap(int size,Collection<CouchbaseConstants.DocumentFlag> flags,boolean isForIncrementalRating){
        this._isIncrementalRating = isForIncrementalRating;
        this._flags = new HashSet<CouchbaseConstants.DocumentFlag>();
        if(flags!=null){
            setFlags(flags);
        }
        addFlag(CouchbaseConstants.DocumentFlag.CdrBucket);
        setCurrDbSize(size);
    }
    
    public boolean isIncrementalRating(){
        return this._isIncrementalRating;
    }
    
    
    public int getGlobalOverheadCounter(){
        int result=0;
        for(T cdr:values()){
            result+=cdr.getOverheadCounter();
        }
        return result;
    }
    
    public int getCurrDbSize(){
        return _currDbSize;
    }
    
    public void setCurrDbSize(int size){
        _currDbSize = size;
    }
    
    public boolean hasFlag(CouchbaseConstants.DocumentFlag flag){
        return _flags.contains(flag);
    }
    
    public Collection<CouchbaseConstants.DocumentFlag> getFlags(){
        return _flags;
    }
    
    public void setFlags(Collection<CouchbaseConstants.DocumentFlag> flags){
        _flags.clear();
        _flags.addAll(flags);
    }
    
    public void addFlags(Collection<CouchbaseConstants.DocumentFlag> flags){
        _flags.addAll(flags);
    }
    
    public void addFlag(CouchbaseConstants.DocumentFlag flag){
        _flags.add(flag);
    }
    
    public void removeFlag(CouchbaseConstants.DocumentFlag flag){
        _flags.remove(flag);
    }
    
    public void removeFlags(Collection<CouchbaseConstants.DocumentFlag> flag){
        _flags.remove(flag);
    }
    
    
    public void add(T cdr){
        put(cdr.getUid(),cdr);
    }
    
    public void remove(T cdr){
        remove(cdr.getUid());
    }
    
    public void addAll(Collection<T> cdrsList){
        for(T cdr:cdrsList){
            add(cdr);
        }
    }
    
    @Override
    public String toString(){
        return 
            "{\n"+
            "    currDbSize : "+_currDbSize+",\n"+
            "    checksum : "+_checkSum+",\n"+
            "    globalOverhead : "+getGlobalOverheadCounter()+",\n"+
            "    flags : "+_flags.toString()+",\n"+
            "    cdrs : [\n"+
            "    "+super.toString()+"\n"+
            "    ]\n"+
            "}\n";
    }
    
}