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

package com.dreameddeath.rating.model.cdr;


import com.dreameddeath.core.couchbase.BinarySerializer;
import com.dreameddeath.core.model.document.BaseCouchbaseDocumentElement;

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
    
    private String uid;
    private T_CDRDATA cdrData;
    private List<T_CDRRATING> ratingResults;
    private boolean isDuplicated;
    private boolean isDiscarded;
    private int overheadCounter;//Maintain a counter of overhead to check if compaction is required
    
    /**
    * The constructor requires a uid
    * @param uid the cdr unique id
    */
    public GenericCdr(String uid){
        this.uid = uid;
        this.isDuplicated = false;
        this.isDiscarded = false;
        this.overheadCounter=0;
        this.cdrData = null;
        this.ratingResults=new ArrayList<T_CDRRATING>();
    }

    /**
    *  Getter for duplicate Flag
    *  @return the duplicated flag
    */
    public boolean isDuplicated(){
        return this.isDuplicated;
    }

    /**
    *  Setter for duplicate Flag
    *  @param isDuplicated the new duplicated flag
    */
    public void setDuplicated(boolean isDuplicated){
        this.isDuplicated=isDuplicated;
    }

/**
    *  Getter for discarded Flag
    *  @return the discarded flag
    */
    public boolean isDiscarded(){
        return this.isDiscarded;
    }

    /**
    *  Setter for discarded Flag
    *  @param isDiscarded the new discarded flag
    */
    public void setDiscarded(boolean isDiscarded){
        this.isDiscarded=isDiscarded;
    }

    
    /**
    *  Getter for the Cdr Unique id (UID)
    *  @return unique id
    */
    public String getUid(){
        return this.uid;
    }

    /**
    *  Getter for the cdr base data in serialized format
    *  @return array of bytes of representing the raw data of the cdr
    */
    public byte[] getCdrDataSerialized(){
        return getCdrDataSerializer().serialize(this.cdrData);
        //return this._cdrData;
    }
    
    /**
    *  Getter for the cdr base data
    *  @return array of bytes of representing the data of the cdr
    */
    public T_CDRDATA getCdrData(){
        return this.cdrData;
    }
    
    
    /**
    *  Setter for the cdr base data
    *  @param data the data of the cdr
    */
    public void setCdrData(T_CDRDATA data){
        this.cdrData=data;
    }
    
    /**
    *  Setter for the cdr base data in serialized format
    *  @param data array of bytes of representing the raw data of the cdr
    */
    public void setCdrDataSerialized(byte[] data){
        this.cdrData=getCdrDataSerializer().deserialize(data);
    }
    
    /**
    *  Getter for rating results in serialized format
    *  @return a List of rating Results in serialized format
    */
    public Collection<byte[]> getRatingResultsSerialized(){
        Collection<byte[]> result = new ArrayList<byte[]>();
        
        if(this.ratingResults!=null){
            for(T_CDRRATING ratingResult:ratingResults){
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
        return ratingResults;
    }
    
    /**
    *  Setter for the rating ordered results (one per rating attempt)
    *  @param ratingResults the list of serialized rating result 
    */
    public void setRatingResultsSerialized(Collection<byte[]> ratingResults){
        this.ratingResults.clear();
        for(byte[] ratingResult:ratingResults){
            this.ratingResults.add(getCdrRatingSerializer().deserialize(ratingResult));
        }
    }
    
    /**
    *  Setter for the rating ordered results (one per rating attempt)
    *  @param ratingResults the list of serialized rating result 
    */
    public void setRatingResults(Collection<T_CDRRATING> ratingResults){
        this.ratingResults.clear();
        for(T_CDRRATING ratingResult:ratingResults){
            this.ratingResults.add(ratingResult);
        }
    }
    
    /**
    *  Appender for a rating result in serialized format
    *  @param ratingResult a serialized rating result to be appended
    */
    public void addRatingResultSerialized(byte[] ratingResult){
        this.ratingResults.add(getCdrRatingSerializer().deserialize(ratingResult));
    }
    
    /**
    *  Appender for a rating result 
    *  @param ratingResult a rating result to be appended
    */
    public void addRatingResult(T_CDRRATING ratingResult){
        this.ratingResults.add(ratingResult);
    }
    
    /**
    *  Increment overhead (when detecting uncompacted raw + rated Cdr separately)
    */
    public void incOverheadCounter(){
        this.overheadCounter++;
    }
    
    /**
    *  Getter for overhead Counter (when detecting uncompacted raw + rated Cdr)
    *  @return the current overhead counter for given cdr
    */
    public int getOverheadCounter(){
        return this.overheadCounter;
    }
    
    /**
    *  Convert to string
    */
    @Override
    public String toString(){
        String result = "{\n\tCdr : <"+getUid()+">\n";
        if(cdrData!=null){
            result += "\t Cdr Data : <"+cdrData.toString()+">\n";
        }
        for(T_CDRRATING rating:ratingResults){
            result += "\t Rating Result : <"+rating.toString()+">\n";
        }
        result+="}";
        return result;
    }
    
}