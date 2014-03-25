package com.dreammedeath.rating.model.context;

import java.util.List;
import java.util.ArrayList;

import org.joda.time.DateTime;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

@JsonInclude(Include.NON_EMPTY)
public class RatingContextCdrBucket{
    private String _uid; //Bucket Unique Id
    private Integer _dbSize; //Size estimation
    private Integer _nbCdrs;

    @JsonProperty("uid")
    public String getUid(){ return _uid;}
    public void setUid(String uid){ this._uid = uid; }
 
    @JsonProperty("dbSize")
    public Integer getDbSize(){ return _dbSize;}
    public void setDbSize(Integer dbSize){ this._dbSize = dbSize; }
 
    @JsonProperty("nbCdrs")
    public Integer getNbCdrs(){ return _nbCdrs;}
    public void setNbCdrs(Integer nbCdrs){ this._nbCdrs = nbCdrs; }
 
}