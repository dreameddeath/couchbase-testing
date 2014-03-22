package com.dreameddeath.rating.storage;


import java.util.List;
import java.util.ArrayList;
/**
   class used to store raw result having as an hypothesis that a cdrs is represented by
   - identified by a unique id
   - a series of bytes for the orig data
   - a series of bytes for each rating attemps (or rerating attempts)
*/
public class RawCdr{
    private String _uid;
    private byte[] _cdrData;
    private List<byte[]> _ratingResults;
    private boolean _isDuplicated;
    private boolean _isDiscarded;
    private Integer _overheadCounter;//Maintain a counter of overhead to check if compaction is required
    
    /**
    * The constructor requires a uid
    * @param uid the cdr unique id
    */
    public RawCdr(String uid){
        this._uid = uid;
        this._isDuplicated = false;
        this._isDiscarded = false;
        this._overheadCounter=0;
    }

    /**
    *  Getter for duplicate Flag
    *  @return the duplicated flag
    */
    public boolean isDuplicated(){
        return this._isDuplicated;
    }

    /**
    *  Setter for duplicate Flag
    *  @param the new duplicated flag
    */
    public void setDuplicated(boolean isDuplicated){
        this._isDuplicated=isDuplicated;
    }

/**
    *  Getter for discarded Flag
    *  @return the discarded flag
    */
    public boolean isDiscarded(){
        return this._isDiscarded;
    }

    /**
    *  Setter for discarded Flag
    *  @param the new discarded flag
    */
    public void setDiscarded(boolean isDiscarded){
        this._isDiscarded=isDiscarded;
    }

    
    /**
    *  Getter for the Cdr Unique id (UID)
    *  @return unique id
    */
    public String getUid(){
        return this._uid;
    }

    /**
    *  Getter for the cdr base data
    *  @return array of bytes of representing the raw data of the cdr
    */
    public byte[] getCdrData(){
        return this._cdrData;
    }
    
    /**
    *  Setter for the cdr base data
    *  @param data array of bytes of representing the raw data of the cdr
    */
    public void setCdrData(byte[] data){
        this._cdrData=data;
    }
    
    
    /**
    *  Getter for rating results
    *  @return a List of rating Results
    */
    public List<byte[]> getRatingResults(){
        if(this._ratingResults==null){ return new ArrayList<byte[]>();}
        return this._ratingResults;
    }
    
    /**
    *  Setter for the rating ordered results (one per rating attempt)
    *  @param ratingResults 
    */
    public void setRatingResults(List<byte[]> ratingResults){
        this._ratingResults=ratingResults;
    }
    
    /**
    *  Appender for a rating result
    *  @param ratingResult a rating result to be appended
    */
    public void addRatingResult(byte[] ratingResults){
        if(this._ratingResults == null){
            this._ratingResults = new ArrayList<byte[]>();
        }
        this._ratingResults.add(ratingResults);
    }
    
    /**
    *  Increment overhead (when detecting uncompacted raw + rated Cdr separately)
    */
    public void incOverheadCounter(){
        this._overheadCounter++;
    }
    
    /**
    *  Getter for overhead Counter (when detecting uncompacted raw + rated Cdr)
    *  @return the current overhead counter for given cdr
    */
    public int getOverheadCounter(){
        return this._overheadCounter++;
    }
    
    /**
    *  Convert to string
    */
    @Override
    public String toString(){
        String result = "{\n\tCdr : <"+getUid()+">\n";
        if(_cdrData!=null){
            result += "\t Raw Data : <"+new String(_cdrData)+">\n";
        }
        for(byte[] rating:getRatingResults()){
            result += "\t Rating Result : <"+new String(rating)+">\n";
        }
        result+="}";
        return result;
    }
    
}