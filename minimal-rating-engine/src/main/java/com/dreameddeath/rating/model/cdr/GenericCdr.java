/*
 * Copyright Christophe Jeunesse
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package com.dreameddeath.rating.model.cdr;


import com.dreameddeath.core.model.document.BaseCouchbaseDocumentElement;
import com.dreameddeath.core.storage.BinarySerializer;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
/**
   class used to store raw result having as an hypothesis that a cdrs is represented by
   - identified by a unique id
   - a series of bytes for the orig data
   - a series of bytes for each rating attempts (or rerating attempts)
*/
public abstract class GenericCdr<T_CDRDATA,T_CDRRATING> extends BaseCouchbaseDocumentElement{
    ///abstract method which should return the Serializer of the Cdr Data Part
    protected abstract BinarySerializer<T_CDRDATA> getCdrDataSerializer();
    ///abstract method which should return the Serializer of the Cdr Rating Result Part
    protected abstract BinarySerializer<T_CDRRATING> getCdrRatingSerializer();
    
    private String _uid;
    private T_CDRDATA _cdrData;
    private List<T_CDRRATING> _ratingResults;
    private boolean _isDuplicated;
    private boolean _isDiscarded;
    private int _overheadCounter;//Maintain a counter of overhead to check if compaction is required
    
    /**
    * The constructor requires a uid
    * @param uid the cdr unique id
    */
    public GenericCdr(String uid){
        this._uid = uid;
        this._isDuplicated = false;
        this._isDiscarded = false;
        this._overheadCounter=0;
        this._cdrData = null;
        this._ratingResults=new ArrayList<T_CDRRATING>();
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
    *  @param isDuplicated the new duplicated flag
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
    *  @param isDiscarded the new discarded flag
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
    *  Getter for the cdr base data in serialized format
    *  @return array of bytes of representing the raw data of the cdr
    */
    public byte[] getCdrDataSerialized(){
        return getCdrDataSerializer().serialize(this._cdrData);
        //return this._cdrData;
    }
    
    /**
    *  Getter for the cdr base data
    *  @return array of bytes of representing the data of the cdr
    */
    public T_CDRDATA getCdrData(){
        return this._cdrData;
    }
    
    
    /**
    *  Setter for the cdr base data
    *  @param data the data of the cdr
    */
    public void setCdrData(T_CDRDATA data){
        this._cdrData=data;
    }
    
    /**
    *  Setter for the cdr base data in serialized format
    *  @param data array of bytes of representing the raw data of the cdr
    */
    public void setCdrDataSerialized(byte[] data){
        this._cdrData=getCdrDataSerializer().deserialize(data);
    }
    
    /**
    *  Getter for rating results in serialized format
    *  @return a List of rating Results in serialized format
    */
    public Collection<byte[]> getRatingResultsSerialized(){
        Collection<byte[]> result = new ArrayList<byte[]>();
        
        if(this._ratingResults!=null){
            for(T_CDRRATING ratingResult:_ratingResults){
                result.add(getCdrRatingSerializer().serialize(ratingResult));
            }
        }
        return result;
    }
    
    /**
    *  Getter for rating results 
    *  @return a List of rating Results
    */
    public Collection<T_CDRRATING> getRatingResults(){
        return _ratingResults;
    }
    
    /**
    *  Setter for the rating ordered results (one per rating attempt)
    *  @param ratingResults the list of serialized rating result 
    */
    public void setRatingResultsSerialized(Collection<byte[]> ratingResults){
        this._ratingResults.clear();
        for(byte[] ratingResult:ratingResults){
            this._ratingResults.add(getCdrRatingSerializer().deserialize(ratingResult));
        }
    }
    
    /**
    *  Setter for the rating ordered results (one per rating attempt)
    *  @param ratingResults the list of serialized rating result 
    */
    public void setRatingResults(Collection<T_CDRRATING> ratingResults){
        this._ratingResults.clear();
        for(T_CDRRATING ratingResult:ratingResults){
            this._ratingResults.add(ratingResult);
        }
    }
    
    /**
    *  Appender for a rating result in serialized format
    *  @param ratingResult a serialized rating result to be appended
    */
    public void addRatingResultSerialized(byte[] ratingResult){
        this._ratingResults.add(getCdrRatingSerializer().deserialize(ratingResult));
    }
    
    /**
    *  Appender for a rating result 
    *  @param ratingResult a rating result to be appended
    */
    public void addRatingResult(T_CDRRATING ratingResult){
        this._ratingResults.add(ratingResult);
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
        return this._overheadCounter;
    }
    
    /**
    *  Convert to string
    */
    @Override
    public String toString(){
        String result = "{\n\tCdr : <"+getUid()+">\n";
        if(_cdrData!=null){
            result += "\t Cdr Data : <"+_cdrData.toString()+">\n";
        }
        for(T_CDRRATING rating:_ratingResults){
            result += "\t Rating Result : <"+rating.toString()+">\n";
        }
        result+="}";
        return result;
    }
    
}